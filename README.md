# Simple Game Networking

Simple Game Networking is a backend server designed for multiplayer games. Built with **Ktor** and **Protocol Buffers (Protobuf)**, it aims to provide a lightweight and scalable solution for real-time game communication.

## TODO
- [ ] Refactor packet system to send only changes instead of state
- [ ] Create proper player authorization and packet validations
- [ ] Add physics
- [ ] Containerization and kubernetes integration

## Technologies Used

- **Kotlin**: A concise and expressive programming language for backend development.
- **Ktor**: A flexible framework for building asynchronous servers.
- **Protobuf**: A high-performance serialization library.
- **Gradle**: For dependency management and build automation.

## Prerequisites

To run this project, ensure you have the following installed:
- **JDK 21** or higher
- **Gradle 8.0** or higher
- **Protobuf Compiler** (`protoc`)

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/simple-game-networking.git
   cd simple-game-networking
   ```
2. Generate Protobuf classes:
    ```bash
   ./gradlew generateProto
   ```
3. Run the server:
    ```bash
   ./gradlew run
   ```

