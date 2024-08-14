# grmskyi-twitter-project
This project is a microservice-based system designed for user authentication and authorization, leveraging JWT (JSON Web Tokens) for secure communication and RabbitMQ for messaging. The services handle user registration, login, logout, and token-based authentication, ensuring security and scalability.In addition, it has the functionality  to manage user interactions, subscriptions, and feeds in a scalable and efficient manner. Below is a detailed description of the key microservices in this project.

## Installation

1. Clone the repository:

```bash
    git clone https://github.com/yourusername/grmskyi-twitter-project.git
    cd grmskyi-twitter-project
```

2. Build the project using Maven:

```bash
    mvn clean install
```

3. Run the application:

```bash
    mvn spring-boot:run
```

![Alt text](/screenshots_for_github/project_schema.png?raw=true "Project Schema")

## Docker Compose
```yaml
services:
  mongodb:
    image: mongo
    container_name: mongodb
    ports:
      - "27017:27017"
    volumes:
      - data:/data

  mongo-express:
    image: mongo-express
    container_name: mongo-express
    restart: always
    ports:
      - "8081:8081"
    environment:
      ME_CONFIG_MONGODB_URL: mongodb://localhost:27017/
      ME_CONFIG_MONGODB_SERVER: mongodb
      ME_CONFIG_BASICAUTH: false
  rabbitmq:
    image: rabbitmq:3.10.7-management
    hostname: rabbitmq
    restart: unless-stopped
    environment:
      - RABBITMQ_DEFAULT_USER=user
      - RABBITMQ_DEFAULT_PASS=sa
      - RABBITMQ_DEFAULT_VHOST=vhost
      - RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS=-rabbit log_levels [{connection,error},{default,error}]
    volumes:
      - ./rabbitmq:/var/lib/rabbitmq
    ports:
      - "15672:15672"
      - "5672:5672"
    healthcheck:
      test: [ "CMD-SHELL", "rabbitmqctl status" ]
      interval: 30s
      timeout: 10s
      retries: 5
  user-auth-service:
    build:
      context: ./user-auth-service
      dockerfile: Dockerfile
    ports:
      - "8085:8085"
    environment:
      - SPRING_RABBITMQ_HOST=rabbitmq
      - SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/user_auth_db
    depends_on:
      - rabbitmq
      - mongodb
  user-service:
    build:
      context: ./user-service
      dockerfile: Dockerfile
    ports:
      - "8082:8082"
    environment:
      - SPRING_RABBITMQ_HOST=rabbitmq
      - SPRING_DATA_MONGODB_URI=mongodb://mongodb:27017/user_data_db
    depends_on:
      - rabbitmq
      - mongodb
volumes:
  data: { }
```
## user-auth-service
### Purpose
The **user-auth-service** is responsible for handling user authentication, including registration, login, and logout functionalities. It uses JWT tokens for secure communication and RabbitMQ for messaging to other services.

### Core Components
#### RabbitMQ Configuration

The `RabbitMQConfig` and `RabbitMQSenderConfig` classes configure RabbitMQ message converters, queues, exchanges, and bindings.
`RabbitTemplate` is customized with a JSON message converter (`Jackson2JsonMessageConverter`) to ensure messages are serialized and deserialized in JSON format.
A Queue, Exchange, and Binding are defined for the RabbitMQ messaging setup, with routing keys specified in the application properties.

#### JWT Authentication Filter

The `JwtAuthenticationFilter` is a critical security component that intercepts HTTP requests to validate JWT tokens.
It processes the token from the Authorization header, extracts user information, and sets up the security context if the token is valid.

The filter also handles any authentication errors by setting the response status to 401 Unauthorized.
#### Security Configuration

The `SecurityConfig` class defines the security settings for the microservice, including CSRF protection, session management, and request authorization.
The configuration ensures that authentication is stateless (using JWT) and that certain paths (like Swagger UI) are excluded from security filters.

#### Authentication Controller

The `AuthenticationController` provides REST APIs for user registration, login, and logout.
It returns JWT tokens upon successful login or registration, which clients use to authenticate subsequent requests.
The controller also handles logout by invalidating sessions and removing JWT cookies.

#### Global Exception Handling

The `CustomGlobalExceptionHandler` handles validation errors and other exceptions globally, ensuring consistent error responses across the service.

### DockerFile
```dockerfile
# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-22 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the production image
FROM eclipse-temurin:22-jdk-alpine AS prod
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/user-auth-service.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "/app/user-auth-service.jar"]
```

### Usage
 - **User Registration**: New users can register by sending their credentials to the /api/v1/auth/register endpoint.
   ![Alt text](/screenshots_for_github/register_user.png?raw=true "Register User")
 - **User Login**: Registered users can log in via the /api/v1/auth/login endpoint, receiving a JWT token upon successful authentication.
   ![Alt text](/screenshots_for_github/login_user.png?raw=true "Login User")
 - **User Logout**: Users can log out by sending a request to /api/v1/auth/logout, which invalidates their session and removes the JWT cookie.
   ![Alt text](/screenshots_for_github/logout_user.png?raw=true "Logout User")

### Swagger API endpoints
You can view the existing and available Endpoints here after launching the project: http://localhost:8085/swagger-ui/index.html#/
![Alt text](/screenshots_for_github/swagger_auth_endpoints.png?raw=true "Swagger Open Api")

### Confirmation that user data is stored in the database
![Alt text](/screenshots_for_github/auth_mongodb_data.png?raw=true "Data from db")

## user-service
### Purpose
The **user-service** is responsible for handling user interactions such as commenting, liking posts, managing user subscriptions, and retrieving user feeds. It also processes messages from RabbitMQ for user data synchronization.

### Core Components
#### RabbitMQ Data Receiver Configuration
The `RabbitMQDataUserReceiverConfig` class configures the RabbitMQ listener and queue.
It listens for messages on a specified queue, logs the received `UserDTO` data, and forwards the data to the `SubscriptionService` for further processing.

#### Interaction Controller
The `InteractionController` provides REST APIs for managing user interactions like commenting on posts and liking or unliking posts.
Key endpoints include:
 - **Post Comment**: Adds a comment to a specific post.
 - **Delete Comment**: Deletes a comment by its ID.
 - **Add Like**: Adds a like to a specific post.
 - **Remove Like**: Removes a like from a specific post.

#### User Subscription Controller
The `UserSubscriptionController` manages user subscriptions, allowing users to follow and retrieve followers.
Key endpoints include:
- **Get Followers**: Retrieves the list of users following the specified user.
- **Follow User**: Allows a user to follow another user.

#### User Feed Controller
The `UserFeedController` handles operations related to user feeds, allowing users to view posts from users they follow and create new posts.
Key endpoints include:
- **Get User Feed**: Retrieves the feed for a user, showing posts from users they follow.
- **Get Specific User Feed**: Retrieves posts created by a specific user.
- **Create Post**: Allows users to create a new post in their feed.

#### FeedServiceImpl
* **Manages the creation** of posts and retrieval of user feeds.
* **Create Post**: Allows users to create new posts with content and a publication date.
* **Get User Feed**: Retrieves a feed showing posts from users the current user follows, ordered by publication date.
* **Get Specific User Feed**: Retrieves posts created by a specific user.

#### InteractionServiceImpl
* **Handles user interactions** such as adding, deleting comments, and managing likes on posts.
* **Add Comment**: Adds a comment to a post and saves it in the database.
* **Delete Comment**: Deletes a comment by its ID.
* **Add Like**: Adds a like to a post if the user has not already liked it, otherwise throws an exception.
* **Remove Like**: Removes a like from a post if it exists, otherwise throws an exception.

#### SubscriptionServiceImpl
* **Manages user subscriptions**, such as following other users and retrieving followers.
* **Get Data from MQ**: Processes user data received from RabbitMQ messages.
* **Get Followers**: Retrieves a list of users who are following a specific user.
* **Follow User**: Creates a subscription for one user to follow another, with validation to prevent duplicate follows.

### DockerFile
```dockerfile
# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-22 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the production image
FROM eclipse-temurin:22-jdk-alpine AS prod
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/user-service.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "/app/user-service.jar"]
```

### Usage
* **Comment on Posts**: Users can comment on posts via the /api/v1/interactions/comment/{postId} endpoint.
  ![Alt text](/screenshots_for_github/writing_comment.png?raw=true "Comment impl")
* **Like/Unlike Posts**: Users can like or unlike posts through the /api/v1/interactions/like/{postId} and /api/v1/interactions/like/{postId} endpoints.
  ![Alt text](/screenshots_for_github/leaving_like.png?raw=true "Like impl")
* **View Feeds**: Users can view their feed and posts from specific users via the /api/v1/feed/{userId} and /api/v1/feed/specific/{userId} endpoints.
  ![Alt text](/screenshots_for_github/get_all_post_from_user_feed.png?raw=true "Get all post from user feed")
  ![Alt text](/screenshots_for_github/see_post_from_followed_user.png?raw=true "Get post from followed feed")
* **Manage Subscriptions**: Users can follow others and retrieve their list of followers through the /api/v1/subscription/{userId}/followers and /api/v1/subscription/{userId}/follow/{followerId} endpoints.
  ![Alt text](/screenshots_for_github/follow_user.png?raw=true "Follow user")
  ![Alt text](/screenshots_for_github/get_followers.png?raw=true "Get followers")

### Swagger API endpoints
You can view the existing and available Endpoints here after launching the project: http://localhost:8082/swagger-ui/index.html#/
![Alt text](/screenshots_for_github/swagger_user_endpoints.png?raw=true "Swagger Open Api")

### Confirmation that user data is stored in the database
![Alt text](/screenshots_for_github/user_comments.png?raw=true "Data from db")
![Alt text](/screenshots_for_github/user_likes.png?raw=true "Data from db")
![Alt text](/screenshots_for_github/user_ports.png?raw=true "Data from db")
![Alt text](/screenshots_for_github/user_subscription.png?raw=true "Data from db")