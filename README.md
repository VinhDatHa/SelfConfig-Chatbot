>[Info] This is a project that a place where I have practiced and learnt Kotlin Multiplatform, it targets Android, iOS and Desktop.
> It still contains errors and bugs due to the lack of fully support in iOS and Desktop version.
> It contains several subfolders:
>  - `/commonMain` is for code that’s common for all targets.
>  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.
> Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…


# SelfConfig-Chatbot 
It's a chat bot app that support multi AI providers, through your API key. Currently, I have pre-defined three providers: [Together AI](https://www.together.ai/), [Open Router](https://www.openrouter.ai), [Open AI](http://openai.com). 
I also want to give a shout-out to [Rikka](https://rikka-ai.com/) — the author of the original code and you can download the fully version in his/her site. I was inspired by this source code to use and further develop it for this project. 

# Tech stack & Open-source libraries
- [Kotlin multi-platform](https://kotlinlang.org/docs/multiplatform.html) based, [Coroutines](https://github.com/Kotlin/kotlinx.coroutines) + [Flow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/) for asynchronous.
- Jetpack
  - Jetpack Compose: Android's modern toolkit for declarative UI development.
  - Lifecycle: Observes Android lifecyles and manages UI states up lifecycle changes.
  - ViewModel: Manages UI-related data and is lifecycle-aware, ensuring data survival through configuration changes.
  - Compose Navigation: Facilitates screen navigation
  - Room: Constructs a database with an SQLite abstraction layer for seamless database access.
  - DataStore: data storage solution that allows to store key-value pairs or typed objects.
  - [Koin](https://insert-koin.io/): Facilitates dependecy injection
- Architecture:
  - MVVM Architecture (View - View Model - Model): Facilitates separation of concerns and promotes maintainability.
  - Repository pattern: Acts as a mediator between different data sources and the application's business logic
- [Ktor](https://ktor.io/): Multiplatform constructs REST APIs and faciliates network data retrieval
- [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization): Kotlin multiplatform / multi-format reflectionless serialization.
- [KSP](https://github.com/google/ksp): Kotlin Symbol Processing API for code generation and analysis.
- [Coil](https://coil-kt.github.io/coil/compose): Multiplatform to load and display iamge.
- [Calf](https://github.com/MohamedRejeb/Calf): Kotlin multiplatform library to pick image from gallery.
- [Jetbrains Markdown](https://www.jetbrains.com/help/idea/markdown.html): to parse and convert markdown to displayable text

# How to use: 
- **Prerequisite:** The API key of pre-define providers include: TogetherAI, Open Router, OpenAI. 
- **Open settings:** Select the `provider section`, enter your API key into field. Pick the a specific model from `Provider` and now, you start using app. 

