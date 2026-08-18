package com.example.myapplicationwd

import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    private var precioDolarPEN: Double = 0.0
    private var precioWorldcoinUSD: Double = 0.0

    private lateinit var tvDolarHoy: TextView
    private lateinit var tvAlertaPrincipal: TextView
    private lateinit var tv5A: TextView
    private lateinit var tv3A: TextView
    private lateinit var tv1A: TextView
    private lateinit var tv30D: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvDolarHoy = findViewById(R.id.tvDolarHoy)
        tvAlertaPrincipal = findViewById(R.id.tvAlertaPrincipal)
        tv5A = findViewById(R.id.tvVentana5A)
        tv3A = findViewById(R.id.tvVentana3A)
        tv1A = findViewById(R.id.tvVentana1A)
        tv30D = findViewById(R.id.tvVentana30D)

        val btnPrecioDolar = findViewById<Button>(R.id.btnPrecioDolar)
        val btnPrecioWld = findViewById<Button>(R.id.btnPrecioWld)
        val btnCompraDolar = findViewById<Button>(R.id.btnCompraDolar)
        val btnVentaDolar = findViewById<Button>(R.id.btnVentaDolar)
        val btnVentaWld = findViewById<Button>(R.id.btnVentaWld)

        btnPrecioDolar.setOnClickListener {
            tvDolarHoy.text = "Consultando Dólar..."
            obtenerDolarEnVivo { precioPen ->
                if (precioPen != null) {
                    precioDolarPEN = precioPen
                    tvDolarHoy.text = "Dólar Hoy: S/ $precioDolarPEN PEN"
                    tvAlertaPrincipal.text = "Precio actualizado en vivo"
                } else mostrarError()
            }
        }

        btnPrecioWld.setOnClickListener {
            tvDolarHoy.text = "Consultando Worldcoin..."
            obtenerWorldcoinEnVivo { precioWld ->
                if (precioWld != null) {
                    precioWorldcoinUSD = precioWld
                    tvDolarHoy.text = "Worldcoin Hoy: $$precioWorldcoinUSD USD"
                    tvAlertaPrincipal.text = "Precio actualizado en vivo"
                } else mostrarError()
            }
        }

        btnCompraDolar.setOnClickListener {
            if (precioDolarPEN > 0.0) evaluarDolarCompra(precioDolarPEN)
            else obtenerDolarEnVivo { p -> if (p != null) { precioDolarPEN = p; evaluarDolarCompra(p) } else mostrarError() }
        }

        btnVentaDolar.setOnClickListener {
            if (precioDolarPEN > 0.0) evaluarDolarVenta(precioDolarPEN)
            else obtenerDolarEnVivo { p -> if (p != null) { precioDolarPEN = p; evaluarDolarVenta(p) } else mostrarError() }
        }

        btnVentaWld.setOnClickListener {
            if (precioWorldcoinUSD > 0.0) evaluarVentaWorldcoin(precioWorldcoinUSD)
            else obtenerWorldcoinEnVivo { p -> if (p != null) { precioWorldcoinUSD = p; evaluarVentaWorldcoin(p) } else mostrarError() }
        }
    }

    private fun obtenerDolarEnVivo(onResult: (Double?) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://open.er-api.com/v6/latest/USD")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val responseText = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(responseText)
                val penRate = json.getJSONObject("rates").getDouble("PEN")
                val precioRedondeado = String.format(Locale.US, "%.2f", penRate).toDouble()

                withContext(Dispatchers.Main) { onResult(precioRedondeado) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(null) }
            }
        }
    }

    private fun obtenerWorldcoinEnVivo(onResult: (Double?) -> Unit) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val url = URL("https://api.binance.com/api/v3/ticker/price?symbol=WLDUSDT")
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                val responseText = conn.inputStream.bufferedReader().readText()
                val json = JSONObject(responseText)
                val price = json.getString("price").toDouble()
                val precioRedondeado = String.format(Locale.US, "%.2f", price).toDouble()

                withContext(Dispatchers.Main) { onResult(precioRedondeado) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onResult(null) }
            }
        }
    }

    private fun evaluarDolarCompra(precioDolar: Double) {
        val minDin = viewModel.dolarMinimo.value
        val c5a = precioDolar <= minDin
        val c3a = precioDolar <= minDin
        val c1a = precioDolar <= minDin
        val c30d = precioDolar <= minDin

        tvDolarHoy.text = "Dólar Compra: S/ $precioDolar"
        actualizarTextosVentanas(c5a, c3a, c1a, c30d, "S/ $minDin", "Mínimo")
        tvAlertaPrincipal.text = if (c30d) "¡Alerta de COMPRA de Dólares!" else "Sin alerta de Compra"
    }

    private fun evaluarDolarVenta(precioDolar: Double) {
        val maxDin = viewModel.dolarMaximo.value
        val c5a = precioDolar >= maxDin
        val c3a = precioDolar >= maxDin
        val c1a = precioDolar >= maxDin
        val c30d = precioDolar >= maxDin

        tvDolarHoy.text = "Dólar Venta: S/ $precioDolar"
        actualizarTextosVentanas(c5a, c3a, c1a, c30d, "S/ $maxDin", "Máximo")
        tvAlertaPrincipal.text = if (c30d) "¡Alerta de VENTA de Dólares!" else "Sin alerta de Venta"
    }

    private fun evaluarVentaWorldcoin(precioWld: Double) {
        val maxWld = viewModel.wldMaximo.value
        val c5a = precioWld >= maxWld
        val c3a = precioWld >= maxWld
        val c1a = precioWld >= maxWld
        val c30d = precioWld >= maxWld

        tvDolarHoy.text = "WLD Venta: $$precioWld USD"
        actualizarTextosVentanas(c5a, c3a, c1a, c30d, "$$maxWld", "Máximo")
        tvAlertaPrincipal.text = if (c30d) "¡Alerta de VENTA Worldcoin!" else "Sin alerta de Venta WLD"
    }

    private fun actualizarTextosVentanas(
        c5a: Boolean, c3a: Boolean, c1a: Boolean, c30d: Boolean,
        valor: String, tipo: String
    ) {
        tv5A.text = "• $tipo 5 años ($valor): " + if (c5a) "✅ SÍ" else "❌ NO"
        tv3A.text = "• $tipo 3 años ($valor): " + if (c3a) "✅ SÍ" else "❌ NO"
        tv1A.text = "• $tipo 1 año  ($valor):  " + if (c1a) "✅ SÍ" else "❌ NO"
        tv30D.text = "• $tipo 30 días ($valor):  " + if (c30d) "✅ SÍ" else "❌ NO"

        val colorVerde = Color.parseColor("#2E7D32")
        tv5A.setTextColor(if (c5a) colorVerde else Color.BLACK)
        tv3A.setTextColor(if (c3a) colorVerde else Color.BLACK)
        tv1A.setTextColor(if (c1a) colorVerde else Color.BLACK)
        tv30D.setTextColor(if (c30d) colorVerde else Color.BLACK)
    }

    private fun mostrarError() {
        tvDolarHoy.text = "Error de conexión"
        Toast.makeText(this, "No se pudo obtener el precio en vivo.", Toast.LENGTH_SHORT).show()
    }
}