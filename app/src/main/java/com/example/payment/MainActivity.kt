package com.example.payment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class MainActivity : AppCompatActivity(), PaymentResultListener {

    companion object {
        private const val RAZORPAY_KEY_ID = "rzp_test_xxxxxxxx" // Replace with your Razorpay key
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Preload Razorpay Checkout
        Checkout.preload(applicationContext)

        val editTextAmount: EditText = findViewById(R.id.editTextAmount)
        val payButton: Button = findViewById(R.id.buttonPay)

        payButton.setOnClickListener {
            val amount = editTextAmount.text.toString().trim()
            if (amount.isNotEmpty()) {
                startUPIPayment(amount, "renkundan-1@okaxis", "Renu Kundan", "Payment for testing")
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // UPI Payment Function
    private fun startUPIPayment(amount: String, upiId: String, name: String, note: String) {
        val uri = Uri.parse("upi://pay").buildUpon()
            .appendQueryParameter("pa", upiId)
            .appendQueryParameter("pn", name)
            .appendQueryParameter("tn", note)
            .appendQueryParameter("am", amount)
            .appendQueryParameter("cu", "INR")
            .build()

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = uri
        }

        val chooser = Intent.createChooser(intent, "Pay with UPI")
        if (chooser.resolveActivity(packageManager) != null) {
            startActivityForResult(chooser, 100)
        } else {
            Toast.makeText(this, "No UPI app found", Toast.LENGTH_SHORT).show()
        }
    }

    // Result from UPI Payment
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {
            val response = data?.getStringExtra("response") ?: "No response"
            Toast.makeText(this, "UPI Response: $response", Toast.LENGTH_LONG).show()
            // You can parse response here to determine transaction status
        } else {
            Toast.makeText(this, "Transaction cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Razorpay Payment (optional)
    private fun startRazorpayPayment() {
        val activity: Activity = this
        val checkout = Checkout().apply {
            setKeyID(RAZORPAY_KEY_ID)
        }

        try {
            val options = JSONObject().apply {
                put("name", "Your App Name")
                put("description", "Test Payment")
                put("currency", "INR")
                put("amount", "100") // Amount in paise (100 = ₹1)

                val prefill = JSONObject().apply {
                    put("email", "vickykumarsahu178@gmail.com")
                    put("contact", "7493882135")
                }

                put("prefill", prefill)
            }
            checkout.open(activity, options)
        } catch (e: Exception) {
            Log.e("PaymentError", "Error in starting Razorpay Checkout", e)
        }
    }

    // Razorpay Callbacks
    override fun onPaymentSuccess(razorpayPaymentID: String) {
        Toast.makeText(this, "Payment successful: $razorpayPaymentID", Toast.LENGTH_SHORT).show()
    }

    override fun onPaymentError(code: Int, response: String) {
        Toast.makeText(this, "Payment failed: $response", Toast.LENGTH_SHORT).show()
    }
}
