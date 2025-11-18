# AI Integration Guide

Complete guide for integrating AI capabilities (ChatGPT-style chat) into your Kotlin Compose Multiplatform app.

## ✨ What's Included

The boilerplate now includes a **complete AI chat feature** that works across all platforms:

- ✅ **ChatGPT-style interface** with message bubbles
- ✅ **Multiple AI providers** support (OpenAI, Anthropic, local models)
- ✅ **Clean Architecture** implementation
- ✅ **Real-time chat** with typing indicators
- ✅ **Model selection** (GPT-3.5, GPT-4, etc.)
- ✅ **API key management** (secure dialog)
- ✅ **Error handling** with user feedback
- ✅ **Works on Android, iOS, Desktop**

## 🚀 Quick Start

### 1. Get an API Key

**OpenAI (Recommended):**
1. Go to https://platform.openai.com/api-keys
2. Sign up or log in
3. Click "Create new secret key"
4. Copy the key (starts with `sk-...`)

**Alternative Providers:**
- **Anthropic Claude**: https://console.anthropic.com/
- **Google Gemini**: https://makersuite.google.com/app/apikey
- **Local Models**: Use LM Studio or Ollama (no API key needed)

### 2. Navigate to AI Chat

Add navigation from your main screen:

```kotlin
// In SampleScreens.kt or your navigation file
import com.example.kmpcleanarch.presentation.navigation.AiChatScreenVoyager

// Add a button to navigate to AI chat
Button(onClick = {
    navigator.push(AiChatScreenVoyager())
}) {
    Text("AI Assistant")
}
```

### 3. Enter Your API Key

1. Open the AI Chat screen
2. Tap the settings icon (⚙️) in the top right
3. Select "Set API Key"
4. Paste your OpenAI API key
5. Tap "Save"

### 4. Start Chatting!

Type a message and press send. The AI will respond in real-time!

## 🏗️ Architecture

Following Clean Architecture, the AI feature is organized in three layers:

### Domain Layer

```
domain/
├── model/
│   └── AiMessage.kt          # Chat message entity
├── repository/
│   └── AiRepository.kt       # AI operations interface
└── usecase/
    ├── SendAiMessageUseCase.kt
    └── GetAvailableAiModelsUseCase.kt
```

**Entities:**
- `AiMessage` - Chat message with role (user/assistant)
- `AiChatSession` - Chat session with history
- `AiModel` - AI model configuration
- `AiProvider` - Enum of supported providers

### Data Layer

```
data/
├── remote/
│   ├── api/
│   │   └── AiApi.kt          # OpenAI-compatible API client
│   └── dto/
│       └── AiDto.kt          # API request/response DTOs
└── repository/
    └── AiRepositoryImpl.kt   # Implementation
```

**API Implementation:**
- OpenAI-compatible REST API
- JSON serialization with Kotlinx Serialization
- Ktor HTTP client for all platforms
- Error handling and retry logic

### Presentation Layer

```
presentation/
├── state/
│   └── AiChatUiState.kt      # Chat UI state
├── viewmodel/
│   └── AiChatViewModel.kt    # Chat logic
├── ui/screen/
│   └── AiChatScreen.kt       # Chat interface
└── navigation/
    └── AiScreens.kt          # Voyager navigation
```

## 🔧 Configuration

### Using Different AI Providers

#### OpenAI (Default)
```kotlin
// Already configured in AppModule.kt
AiApi(
    httpClient = get(),
    baseUrl = AiApi.OPENAI_BASE_URL,  // https://api.openai.com/v1
    apiKey = "" // Set via UI
)
```

#### Anthropic Claude
```kotlin
// Update in AppModule.kt
AiApi(
    httpClient = get(),
    baseUrl = AiApi.ANTHROPIC_BASE_URL,
    apiKey = ""
)

// Update available models in AiRepositoryImpl.kt
AiModel(
    id = "claude-3-opus-20240229",
    name = "Claude 3 Opus",
    provider = AiProvider.ANTHROPIC
)
```

#### Local Models (LM Studio, Ollama)
```kotlin
// For local development - no API key needed!
AiApi(
    httpClient = get(),
    baseUrl = AiApi.LOCAL_LM_STUDIO,  // http://localhost:1234/v1
    apiKey = "not-needed"
)

// Or for Ollama
baseUrl = AiApi.LOCAL_OLLAMA  // http://localhost:11434/v1
```

#### Custom API
```kotlin
AiApi(
    httpClient = get(),
    baseUrl = "https://your-custom-api.com/v1",
    apiKey = "your-key"
)
```

### Secure API Key Storage

The current implementation stores the API key in memory. For production, use secure storage:

**Android:**
```kotlin
// Use EncryptedSharedPreferences
val sharedPreferences = EncryptedSharedPreferences.create(
    context,
    "secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
sharedPreferences.edit().putString("ai_api_key", apiKey).apply()
```

**iOS:**
```kotlin
// Use Keychain
// Implement in iosMain with expect/actual pattern
```

**Desktop:**
```kotlin
// Use OS keystore or encrypted preferences
```

## 💬 Chat Features

### Message Types

```kotlin
// User message
AiMessage(
    content = "Hello, AI!",
    role = MessageRole.USER
)

// AI response
AiMessage(
    content = "Hello! How can I help you?",
    role = MessageRole.ASSISTANT
)

// System message (for context)
AiMessage(
    content = "You are a helpful assistant.",
    role = MessageRole.SYSTEM
)
```

### Conversation History

The chat maintains full conversation history:

```kotlin
val conversationHistory: List<AiMessage> = uiState.messages

// AI uses context from all previous messages
sendAiMessageUseCase(
    message = "What did I ask before?",
    conversationHistory = conversationHistory,
    model = selectedModel
)
```

### Model Selection

Switch between different AI models:

```kotlin
// Available models (configurable in AiRepositoryImpl)
- GPT-3.5 Turbo (fast, cheap)
- GPT-4 (powerful, more expensive)
- GPT-4 Turbo (balanced)

// Select in UI
viewModel.selectModel(gpt4Model)
```

## 🎨 UI Customization

### Change Chat Bubble Colors

```kotlin
// In AiChatScreen.kt - MessageBubble composable
Surface(
    color = if (message.role == MessageRole.USER) {
        Color(0xFF007AFF)  // iOS blue
    } else {
        Color(0xFFE5E5EA)  // Light gray
    }
)
```

### Add Message Timestamps

```kotlin
// In MessageBubble composable
Column {
    Text(message.content)
    Text(
        text = message.timestamp.toFormattedTime(),
        style = MaterialTheme.typography.caption
    )
}
```

### Custom Empty State

```kotlin
// Replace EmptyChatState in AiChatScreen.kt
@Composable
fun CustomEmptyState() {
    Column {
        Image(painter = painterResource("ai_logo.png"))
        Text("Welcome to AI Assistant!")
        Text("Ask me anything!")
    }
}
```

## 🔌 Advanced Features

### Streaming Responses (Real-time typing)

Implement streaming for real-time responses:

```kotlin
// In AiApi.kt
suspend fun streamChatCompletion(request: ChatCompletionRequest): Flow<String> = flow {
    httpClient.preparePost("$baseUrl/chat/completions") {
        contentType(ContentType.Application.Json)
        header("Authorization", "Bearer $apiKey")
        setBody(request.copy(stream = true))
    }.execute { response ->
        val channel = response.body<ByteReadChannel>()
        while (!channel.isClosedForRead) {
            val line = channel.readUTF8Line() ?: continue
            if (line.startsWith("data: ")) {
                val data = line.substring(6)
                if (data != "[DONE]") {
                    val chunk = Json.decodeFromString<StreamChunk>(data)
                    emit(chunk.choices.first().delta.content)
                }
            }
        }
    }
}
```

### Function Calling

Add function calling for AI to use tools:

```kotlin
data class ChatFunction(
    val name: String,
    val description: String,
    val parameters: JsonObject
)

// Example: Weather function
val weatherFunction = ChatFunction(
    name = "get_weather",
    description = "Get current weather for a location",
    parameters = buildJsonObject {
        put("type", "object")
        put("properties", buildJsonObject {
            put("location", buildJsonObject {
                put("type", "string")
            })
        })
    }
)
```

### Save Chat History

Add persistence for chat sessions:

```kotlin
// 1. Create SQLDelight schema
CREATE TABLE ChatSession (
    id TEXT PRIMARY KEY,
    title TEXT NOT NULL,
    createdAt INTEGER NOT NULL
);

CREATE TABLE ChatMessage (
    id TEXT PRIMARY KEY,
    sessionId TEXT NOT NULL,
    content TEXT NOT NULL,
    role TEXT NOT NULL,
    timestamp INTEGER NOT NULL,
    FOREIGN KEY(sessionId) REFERENCES ChatSession(id)
);

// 2. Add repository methods
interface ChatSessionRepository {
    suspend fun saveChatSession(session: AiChatSession)
    suspend fun getChatSessions(): List<AiChatSession>
    suspend fun getChatSession(id: String): AiChatSession?
}

// 3. Update ViewModel to save/load sessions
```

## 🧪 Testing

### Test AI Use Cases

```kotlin
class SendAiMessageUseCaseTest {
    @Test
    fun `should reject empty message`() = runTest {
        val useCase = SendAiMessageUseCase(fakeAiRepository)

        val result = useCase("", emptyList(), testModel)

        assertTrue(result.isFailure)
    }

    @Test
    fun `should send message successfully`() = runTest {
        val useCase = SendAiMessageUseCase(fakeAiRepository)

        val result = useCase("Hello", emptyList(), testModel)

        assertTrue(result.isSuccess)
        assertEquals(MessageRole.ASSISTANT, result.getOrNull()?.role)
    }
}
```

### Test ViewModel

```kotlin
class AiChatViewModelTest {
    @Test
    fun `should add user message when sending`() = runTest {
        viewModel.onInputChanged("Test message")
        viewModel.sendMessage()

        val messages = viewModel.uiState.value.messages
        assertEquals(1, messages.size)
        assertEquals(MessageRole.USER, messages.first().role)
    }
}
```

### Mock AI Repository

```kotlin
class FakeAiRepository : AiRepository {
    override suspend fun sendMessage(
        message: String,
        conversationHistory: List<AiMessage>,
        model: AiModel
    ): Result<AiMessage> {
        return Result.success(
            AiMessage(
                content = "This is a test response",
                role = MessageRole.ASSISTANT
            )
        )
    }
}
```

## 💰 Cost Management

### OpenAI Pricing (as of 2024)

| Model | Input (per 1M tokens) | Output (per 1M tokens) |
|-------|----------------------|----------------------|
| GPT-3.5 Turbo | $0.50 | $1.50 |
| GPT-4 | $30.00 | $60.00 |
| GPT-4 Turbo | $10.00 | $30.00 |

### Reduce Costs

1. **Use GPT-3.5 for simple tasks**
```kotlin
val cheapModel = AiModel(
    id = "gpt-3.5-turbo",
    maxTokens = 500,  // Lower limit
    temperature = 0.5
)
```

2. **Limit conversation history**
```kotlin
// Keep only last 10 messages
val recentHistory = conversationHistory.takeLast(10)
```

3. **Set token limits**
```kotlin
ChatCompletionRequest(
    maxTokens = 500  // Lower = cheaper
)
```

4. **Use local models** (free!)
- LM Studio: https://lmstudio.ai/
- Ollama: https://ollama.ai/

## 🔒 Security Best Practices

1. **Never hardcode API keys**
   ```kotlin
   // ❌ Bad
   val apiKey = "sk-1234..."

   // ✅ Good
   val apiKey = getSecureApiKey()  // From secure storage
   ```

2. **Validate user input**
   ```kotlin
   if (message.length > 10000) {
       return Result.failure(Exception("Message too long"))
   }
   ```

3. **Rate limiting**
   ```kotlin
   // Add cooldown between requests
   private var lastRequestTime = 0L

   if (System.currentTimeMillis() - lastRequestTime < 1000) {
       return Result.failure(Exception("Please wait"))
   }
   ```

4. **Sanitize AI responses**
   ```kotlin
   val sanitizedResponse = aiResponse
       .replace(Regex("<script[^>]*>.*?</script>"), "")
       .trim()
   ```

## 📱 Platform-Specific Notes

### Android
- Works out of the box
- Use EncryptedSharedPreferences for API keys
- Network permissions automatically handled

### iOS
- Works via Ktor Darwin client
- Store API keys in iOS Keychain
- Handle App Transport Security if needed

### Desktop
- Works with OkHttp client
- Store API keys in OS keystore
- Network access unrestricted

## 🚀 Next Steps

1. **Try the AI Chat** - Navigate and test it!
2. **Add your API key** - Get one from OpenAI
3. **Customize the UI** - Match your app's design
4. **Add features** - Streaming, function calling, etc.
5. **Save chat history** - Add persistence
6. **Deploy** - Ship your AI-powered app!

## 📚 Resources

- [OpenAI API Docs](https://platform.openai.com/docs/api-reference)
- [Ktor Client Docs](https://ktor.io/docs/client.html)
- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
- [LM Studio (Local AI)](https://lmstudio.ai/)
- [Ollama (Local AI)](https://ollama.ai/)

## 🆘 Troubleshooting

**"Failed to send message"**
- Check internet connection
- Verify API key is correct
- Check API quota/billing

**"Model not found"**
- Update model ID in AiRepositoryImpl
- Check if model is available for your API key

**"Rate limit exceeded"**
- Wait and retry
- Upgrade API plan
- Switch to different model

**Slow responses**
- Try GPT-3.5 instead of GPT-4
- Reduce maxTokens
- Use local model for instant responses

---

You now have a production-ready AI chat feature in your multiplatform app! 🎉
