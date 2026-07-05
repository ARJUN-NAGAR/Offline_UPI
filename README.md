# Air-UPI Mesh: Secure Offline Mesh Payments

Air-UPI is a conceptual implementation of a secure, offline UPI payment mesh network backend. It simulates a linear ad-hoc BLE (Bluetooth Low Energy) network topology where devices can sign transactions completely offline, propagate them hop-by-hop via gossip protocol, and settle them when any node reaches an internet-enabled bridge.

---

## 🚀 Key Features

* **Hybrid Cryptography Engine**: RSA-OAEP for AES key exchange + AES-GCM payload encryption for secure transaction payloads.
* **Idempotency & Replay Protection**: Secure JVM memory gating coupled with database uniqueness constraints to evict replayed packets.
* **Gossip Protocol Simulator**: Linear BLE network simulation ($A \leftrightarrow B \leftrightarrow C \leftrightarrow D / E$) where packets propagate through devices reducing TTL.
* **Double-Spend & Race Protection**: Database optimistic locking (`@Version`) preventing concurrent balances updates at multiple bridges.
* **Spring Boot AOP Logging**: Latency, result, and bridge auditing via Aspect-Oriented Programming (`IngestionLoggingAspect`).
* **Premium Glassmorphic Dashboard**: Real-time canvas monitoring, topology status, transaction injections, gossip triggers, and transaction log feeds.

---

## 🛠️ Tech Stack

* **Language**: Java 17
* **Framework**: Spring Boot 3.3.5, Spring Security, Spring Data JPA
* **Database**: H2 (In-memory database)
* **Frontend**: HTML5, Thymeleaf, Tailwind CSS (Custom Dark Glassmorphism)
* **Build tool**: Maven

---

## 💻 Running the App Locally

### Prerequisites
* Java 17 or higher installed.

### 1. Build and Run
Start the server using the Maven wrapper:
```powershell
./mvnw spring-boot:run
```
Once the log reads `Started UpiMeshApplication in X.XX seconds`, access the simulation:
* **Dashboard**: [http://localhost:8080/](http://localhost:8080/)
* **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  * **JDBC URL**: `jdbc:h2:mem:upimesh`
  * **User Name**: `SA`
  * **Password**: *(Leave completely blank)*

### 2. Run Tests
Execute the unit and multi-threaded concurrency tests:
```powershell
./mvnw test
```

---

## 🐳 Running with Docker

You can build and deploy the entire application using Docker with zero external dependencies.

### 1. Build the Docker Image
From the root directory of the project, execute:
```bash
docker build -t air-upi-mesh .
```

### 2. Run the Container
Run the container and map the internal port `8080` to your host machine:
```bash
docker run -d -p 8080:8080 --name air-upi-mesh-app air-upi-mesh
```

### 3. Access the Container
Navigate to [http://localhost:8080/](http://localhost:8080/) in your browser.

---

## 🧪 Step-by-Step Simulation Flow

1. **Sign Offline Transaction**:
   * On the dashboard sidebar, enter a transaction (e.g. Charlie pays Alice ₹120).
   * Inject it into the first node (`Device_A`).
   * Click **Sign & Broadcast**.

2. **Gossip Across Mesh**:
   * Click **⚡ Gossip Tick** **3 times**.
   * Watch the packet badge propagate along the canvas:
     * `Device_A` ➔ `Device_B` ➔ `Device_C` ➔ `Device_D` & `Device_E` (Bridges).

3. **Ingest & Settle**:
   * Once the packet reaches Bridges `Device_D` and `Device_E`, click **🚀 Ingest & Settle**.
   * One bridge thread settles the transaction while the other rejected it as a duplicate or concurrent update, protecting the accounting ledger.
