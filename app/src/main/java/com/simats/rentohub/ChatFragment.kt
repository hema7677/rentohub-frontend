package com.simats.rentohub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatFragment : Fragment() {

    private lateinit var rvChat: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    private val GEMINI_API_KEY = "AIzaSyCRfggPu7TxmcXq5omLROhIC8h2tSWCNeQ"

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        rvChat = view.findViewById(R.id.rvChat)
        etMessage = view.findViewById(R.id.etMessage)
        btnSend = view.findViewById(R.id.btnSend)

        chatAdapter = ChatAdapter(messages)
        rvChat.layoutManager = LinearLayoutManager(requireContext()).apply { stackFromEnd = true }
        rvChat.adapter = chatAdapter

        btnSend.setOnClickListener {
            val userText = etMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                sendMessage(userText)
            }
        }

        if (messages.isEmpty()) {
            addMessage(ChatMessage("Hello! Ask me any doubt you have about Rentohub.", false))
        }
    }

    private fun sendMessage(userText: String) {
        addMessage(ChatMessage(userText, true))
        etMessage.setText("")

        val loadingMsg = ChatMessage("Thinking...", false)
        addMessage(loadingMsg)
        val loadingIndex = messages.size - 1

        val model = GenerativeModel(modelName = "gemini-1.5-flash", apiKey = GEMINI_API_KEY)
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = model.generateContent(userText)
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        messages.removeAt(loadingIndex)
                        chatAdapter.notifyItemRemoved(loadingIndex)
                        addMessage(ChatMessage(response.text ?: "I'm having trouble responding.", false))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("ChatFragment", "AI Error: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    if (isAdded) {
                        messages.removeAt(loadingIndex)
                        chatAdapter.notifyItemRemoved(loadingIndex)
                        
                        // Fallback to Local AI
                        val localReply = getLocalResponse(userText)
                        addMessage(ChatMessage(localReply, false))
                    }
                }
            }
        }
    }

    private fun getLocalResponse(userText: String): String {
        val input = userText.lowercase()
        return when {
            input.contains("hi") || input.contains("hello") -> "Hello! I am your Rentohub assistant."
            input.contains("book") -> "To book, click on any item, select dates, and pay via 'Book Now'."
            input.contains("pay") || input.contains("cost") -> "We use Razorpay for UPI, Cards, and Net Banking."
            input.contains("address") -> "Provide your address on the booking screen before payment."
            else -> "I'm the Rentohub bot! Ask me about bookings, cameras, or payments."
        }
    }

    private fun addMessage(message: ChatMessage) {
        messages.add(message)
        chatAdapter.notifyItemInserted(messages.size - 1)
        rvChat.scrollToPosition(messages.size - 1)
    }
}
