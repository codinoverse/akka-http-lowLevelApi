**Low-Level REST API for Book Store**

This project is a low-level REST API for a book store, implemented using Akka HTTP in Scala. The API provides the following functionalities:


**Features**

1. Create Books: The API allows you to create new books and add them to the book store.
2. Fetch Books: You can fetch all the books in the store or fetch a specific book by its ID.
3. Check Book Inventory: The API allows you to check the books that are currently in stock (i.e., have a non-zero quantity).
4. Update Book Inventory: You can update the quantity of a specific book in the store.


**Prerequisites**
1. Scala 2.13.x
2. sbt (Scala build tool)


**Installation**
1. Clone the repository:
git clone https://github.com/codinoverse/akka-http-lowLevelApi.git

2. Navigate to the project directory:
cd low-level-rest-api

3. Build the project:
sbt compile

**Running the API**
To run the API, execute the following command:
1. sbt run
This will start the Akka HTTP server and the book store API will be available at http://localhost:8080/api/books.



**API Endpoints**

**GET /api/books**
Fetches all the books in the store.

**GET /api/books?id=<book_id>**
Fetches a specific book by its ID.

**GET /api/books/inventory?instock=<true|false>**
Fetches the books that are currently in stock (i.e., have a non-zero quantity) or out of stock (i.e., have a zero quantity).

**POST /api/books**
Creates a new book. The request body should be a JSON representation of the book.

**POST /api/books/inventory?id=<book_id>&quantity=<quantity>**
Updates the quantity of a specific book in the store.