# asg-backend-682110168

Backend Development individual assignment — a Spring Boot / Spring Data JPA REST API built on top of the provided `sample-boot-modules` multi-module skeleton (`domain-model`, `web-service`, `web-front`).

## How to run

```
mvn spring-boot:run -pl web-service -am
```

The app uses an in-memory **H2** database (see `web-service/src/main/resources/application.properties`), seeded on startup from `web-service/src/main/resources/data.sql`. The H2 console is available at `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:mem:spring_db`).

To run the tests:

```
mvn test -pl web-service -am
```

## Domain model

Five entities live in the `domain-model` module (`th.mfu.domain`), demonstrating all three required JPA relationship types:

```
Customer 1 ────── 1 ShippingAddress      (One-to-One)
Customer 1 ────── * Order                (One-to-Many / Many-to-One)
Order    1 ────── * OrderItem            (One-to-Many / Many-to-One)
OrderItem * ───── 1 Product              (Many-to-One)
```

- **Customer** — a shop customer (`displayname`, `email`, `phone`, `birthday`). Owns one `ShippingAddress` and can place many `Order`s.
- **ShippingAddress** — a customer's delivery address (`address`, `city`, `postalCode`, `country`). Holds the foreign key back to `Customer` (`customer_id`, unique), making it the owning side of the **one-to-one** relationship.
- **Order** — a sale order (`orderDate`, `status`). Belongs to exactly one `Customer` (**many-to-one**) and contains many `OrderItem`s (**one-to-many**). Mapped to table `orders` since `ORDER` is a reserved SQL keyword.
- **OrderItem** — a line item (`quantity`, `unitPrice`) that belongs to one `Order` and references one `Product` (both **many-to-one**).
- **Product** — a sellable product (`name`, `price`, `description`, `manufactureDate`).

Each entity has a matching DTO in `web-service/.../dto` (`CustomerDTO`, `ShippingAddressDTO`, `OrderDTO`, `OrderItemDTO`, `ProductDTO`) so that JPA entities (with their lazy associations) are never serialized directly over the REST API — this avoids infinite-recursion/lazy-loading issues and keeps the API contract decoupled from the persistence model.

## REST API

Each of the three controllers (`CustomerController`, `ProductController`, `OrderController`) exposes the full CRUD verb set required by the assignment:

| Verb                | Customer                  | Product                  | Order                    |
|----------------------|----------------------------|----------------------------|----------------------------|
| Create (`POST`)      | `/api/customers`           | `/api/products`            | `/api/orders`               |
| List (`GET`)         | `/api/customers`           | `/api/products`            | `/api/orders`               |
| Update (`PATCH`)     | `/api/customers/{id}`      | `/api/products/{id}`       | `/api/orders/{id}`          |
| Delete (`DELETE`)    | `/api/customers/{id}`      | `/api/products/{id}`       | `/api/orders/{id}`          |

`PATCH` endpoints apply a partial update: only non-null fields present in the request body are changed. Creating an `Order` accepts a `customerId` and a list of `{ productId, quantity }` items; the server looks up the customer/products, computes `unitPrice` from the product's current price, and persists the order together with its items in one transaction (exercising the cascading one-to-many relationships).

## Tests

`web-service/src/test/java/th/camt/controller` contains `MockMvc`-based integration tests (`@SpringBootTest` + `@AutoConfigureMockMvc`), one test class per controller, each covering all four REST verbs (Create / List / Update / Delete):

- `CustomerControllerTest`
- `ProductControllerTest`
- `OrderControllerTest` (also exercises the Customer↔Order and Order↔Product relationships)

Tests run against their own isolated in-memory H2 database and each test method runs inside a rolled-back transaction (`@Transactional`), so they don't depend on `data.sql` and don't interfere with each other.

## Project structure

- `domain-model` — JPA entities (`th.mfu.domain`)
- `web-service` — Spring Boot application: repositories, DTOs, services, REST controllers, tests (`th.camt`)
- `web-front` — separate JAX-RS/Jetty front-end module (unchanged, provided by the starter skeleton)

## AI usage disclosure

Claude (Anthropic, Sonnet 5, via Cowork) was used to:

- Design the domain model (entity fields and the One-to-One / Many-to-One / One-to-Many relationships) based on the assignment brief.
- Generate the JPA entities, Spring Data repositories, DTOs, service layer, and REST controllers (Create/List/Patch/Delete) in `domain-model` and `web-service`.
- Rewrite `application.properties` to use H2 and rewrite `data.sql` to match the new schema.
- Write the MockMvc integration tests in `web-service/src/test/java`.
- Draft this README.

All generated code was reviewed manually for correctness. Automated `mvn test` execution could not be run in the assistant's sandboxed environment (no network access to Maven Central), so tests should be (and were intended to be) verified locally with `mvn test` before submission. I can explain any part of this code in person as required.
