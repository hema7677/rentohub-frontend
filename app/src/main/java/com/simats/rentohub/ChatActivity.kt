package com.simats.rentohub

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    private val GEMINI_API_KEY = "AIzaSyCRfggPu7TxmcXq5omLROhIC8h2tSWCNeQ"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        rvChat = findViewById(R.id.rvChat)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(this).apply { stackFromEnd = true }
        rvChat.adapter = chatAdapter

        btnSend.setOnClickListener {
            val userText = etMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                sendMessage(userText)
            }
        }

        addMessage(ChatMessage("Hello! How can I help you today?", false))
    }

    private fun sendMessage(userText: String) {
        // 1. Add user message
        addMessage(ChatMessage(userText, true))
        etMessage.setText("")

        // 2. Add a loading message
        val loadingMsg = ChatMessage("Typing...", false)
        addMessage(loadingMsg)
        val loadingIndex = messages.size - 1

        // 3. Call AI
        // Using "gemini-1.5-flash" which is the most reliable "normal" model currently.
        val model = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = GEMINI_API_KEY)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = model.generateContent(userText)
                withContext(Dispatchers.Main) {
                    messages.removeAt(loadingIndex)
                    chatAdapter.notifyItemRemoved(loadingIndex)
                    addMessage(ChatMessage(response.text ?: "I'm not sure what to say.", false))
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatActivity", "AI Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    messages.removeAt(loadingIndex)
                    chatAdapter.notifyItemRemoved(loadingIndex)
                    
                    // Fallback to Local AI
                    val localReply = getLocalResponse(userText)
                    addMessage(ChatMessage(localReply, false))
                }
            }
        }
    }

    private fun getLocalResponse(userText: String): String {
        val input = userText.lowercase()
        return when {
            input.contains("hi") || input.contains("hello") -> "Hello! I am your Rentohub assistant. How can I help you today?"
            input.contains("book") -> "To book, just click on any equipment from the home screen, select your dates, and click 'Book Now'!"
            input.contains("pay") || input.contains("money") || input.contains("cost") -> "We use Razorpay for secure payments. You can pay via UPI, Card, or Net Banking."
            input.contains("address") || input.contains("ship") -> "You can enter your shipping address on the booking screen before making the payment."
            input.contains("camera") -> "We have elite cameras like Nikon Z6 and Canon EOS available for rent!"
            input.contains("contact") || input.contains("owner") -> "Every product detail page now shows the owner's contact number."
            else -> "I understand you're asking about '$userText'. For the best experience, please ask about booking, payments, or equipment!"
        }
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        chatAdapter.notifyItemInserted(messages.size - 1)
        rvChat.scrollToPosition(messages.size - 1)
    }
}
