# Product Reference Document: Distributed Key-Value Store (DKVS)

This document outlines the engineering roadmap and product specifications for building a high-performance, distributed in-memory cache. The project is structured into five phases, moving from a single-node local cache to a resilient, distributed system.

---

## 1. Executive Summary
**Objective:** Build a scalable, thread-safe, and distributed key-value store optimized for low-latency data retrieval.
**Core Value Prop:** Reduce database load and application latency by providing an intermediate, high-speed memory layer.
**Target Metrics:** * **Latency:** Sub-millisecond for local operations.
* **Availability:** 99.9% (Phase 3+).
* **Scalability:** Linear performance improvement as nodes are added.

---

## 2. Product Roadmap & Phases

### Phase 1: The Core Engine (Single-Node, Non-Persistent)
Build the foundational "brain" of the cache. It must be thread-safe and handle its own memory management.

| Feature | Specification |
| :--- | :--- |
| **Storage Model** | `String` key to `byte[]` or `Object` value. |
| **Concurrency** | Use `ConcurrentHashMap` to handle multi-threaded access without global locks. |
| **Eviction (LRU)** | Evict the Least Recently Used items when memory reaches a defined threshold (e.g., 512MB). |
| **Expiration (TTL)** | Support Time-to-Live per key. Requires a background reaper thread or "lazy deletion" on access. |

### Phase 2: The Service Layer (Networked Single-Node)
Transition from an embedded library to a standalone service.

* **Communication:** Implement a TCP/IP server using **Netty** (Java) or native sockets.
* **Wire Protocol:** Use a simple text-based protocol (like Memcached) or a binary protocol for efficiency.
    * *Example:* `SET <key> <ttl> <length>\r\n<data>\r\n`
* **Performance:** Aim for a "Zero-copy" architecture by reusing `ByteBuffers` to minimize Garbage Collection (GC) overhead.

### Phase 3: The Cluster (Distributed via Consistent Hashing)
Scale horizontally by spreading data across multiple nodes.

* **Consistent Hashing:** Use a hash ring with **virtual nodes** to ensure even distribution and minimize data reshuffling when a node joins or leaves the cluster.
* **Client-Side Logic:** The client library (SDK) calculates which node owns a key using `hash(key) % total_slots`.



### Phase 4: Reliability (Replication)
Ensure that if one node crashes, data is not lost.

* **Primary-Replica Model:** Each key is written to a "Primary" node and asynchronously replicated to $N$ backup nodes (Successor nodes on the hash ring).
* **Consistency Trade-off:** * **Eventual Consistency:** Faster writes, but reads might briefly see old data.
    * **Strong Consistency:** Client waits for acknowledgment from replicas (higher latency).

### Phase 5: Durability (Persistence)
Add "Cold Boot" recovery capabilities.

* **Snapshotting (RDB):** Periodically dump the entire memory state to a point-in-time binary file.
* **Journaling (AOF):** Log every write operation to an "Append-Only File." On restart, replay the log to reconstruct the state.

---

## 3. System Architecture (High-Level)

The system is divided into three logical layers:

1.  **API/Interface:** Handles the protocol parsing (GET/SET/DEL).
2.  **Core Controller:** Manages the LRU Map, Expiry Manager, and Memory Limits.
3.  **Cluster Manager:** Manages the Hash Ring and node health checks (Heartbeats).



---

## 4. Non-Functional Requirements (NFRs)

| Requirement | Target Metric |
| :--- | :--- |
| **Read Latency** | $< 1ms$ (p99) |
| **Write Latency** | $< 5ms$ (p99) with async replication |
| **Fault Tolerance** | No data loss for single-node failure (Replication Factor $\ge 2$) |
| **Memory Efficiency** | Use off-heap memory (Direct Buffers) to avoid Java GC pauses |

---

## 5. Technical Pitfalls to Avoid
* **The Thundering Herd:** Ensure that when a key expires, multiple clients don't all rush to hit the database at once. Implement "Request Coalescing."
* **Lock Contention:** Avoid synchronizing the entire Map. Use "Striped Locking" or `ConcurrentHashMap` segments.
* **Serialization Overhead:** Avoid standard Java Serialization; use **Protobuf** or **Kryo** for faster, smaller payloads.

---

**Next Steps:**
* Should we dive into the **Low-Level Design (LLD)** for the LRU implementation?
* Would you like to see a **Java code skeleton** for the Phase 1 `CacheStore`?


# Product Reference Document: Distributed Key-Value Store (DKVS)

This document outlines the engineering roadmap and product specifications for building a high-performance, distributed in-memory cache. The project is structured into five phases, moving from a single-node local cache to a resilient, distributed system.

---

## 1. Executive Summary
**Objective:** Build a scalable, thread-safe, and distributed key-value store optimized for low-latency data retrieval.
**Core Value Prop:** Reduce database load and application latency by providing an intermediate, high-speed memory layer.
**Target Metrics:** * **Latency:** Sub-millisecond for local operations.
* **Availability:** 99.9% (Phase 3+).
* **Scalability:** Linear performance improvement as nodes are added.

---

## 2. Product Roadmap & Phases

### Phase 1: The Core Engine (Single-Node, Non-Persistent)
Build the foundational "brain" of the cache. It must be thread-safe and handle its own memory management.

| Feature | Specification |
| :--- | :--- |
| **Storage Model** | `String` key to `byte[]` or `Object` value. |
| **Concurrency** | Use `ConcurrentHashMap` to handle multi-threaded access without global locks. |
| **Eviction (LRU)** | Evict the Least Recently Used items when memory reaches a defined threshold (e.g., 512MB). |
| **Expiration (TTL)** | Support Time-to-Live per key. Requires a background reaper thread or "lazy deletion" on access. |

### Phase 2: The Service Layer (Networked Single-Node)
Transition from an embedded library to a standalone service.

* **Communication:** Implement a TCP/IP server using **Netty** (Java) or native sockets.
* **Wire Protocol:** Use a simple text-based protocol (like Memcached) or a binary protocol for efficiency.
    * *Example:* `SET <key> <ttl> <length>\r\n<data>\r\n`
* **Performance:** Aim for a "Zero-copy" architecture by reusing `ByteBuffers` to minimize Garbage Collection (GC) overhead.

### Phase 3: The Cluster (Distributed via Consistent Hashing)
Scale horizontally by spreading data across multiple nodes.

* **Consistent Hashing:** Use a hash ring with **virtual nodes** to ensure even distribution and minimize data reshuffling when a node joins or leaves the cluster.
* **Client-Side Logic:** The client library (SDK) calculates which node owns a key using `hash(key) % total_slots`.



### Phase 4: Reliability (Replication)
Ensure that if one node crashes, data is not lost.

* **Primary-Replica Model:** Each key is written to a "Primary" node and asynchronously replicated to $N$ backup nodes (Successor nodes on the hash ring).
* **Consistency Trade-off:** * **Eventual Consistency:** Faster writes, but reads might briefly see old data.
    * **Strong Consistency:** Client waits for acknowledgment from replicas (higher latency).

### Phase 5: Durability (Persistence)
Add "Cold Boot" recovery capabilities.

* **Snapshotting (RDB):** Periodically dump the entire memory state to a point-in-time binary file.
* **Journaling (AOF):** Log every write operation to an "Append-Only File." On restart, replay the log to reconstruct the state.

---

## 3. System Architecture (High-Level)

The system is divided into three logical layers:

1.  **API/Interface:** Handles the protocol parsing (GET/SET/DEL).
2.  **Core Controller:** Manages the LRU Map, Expiry Manager, and Memory Limits.
3.  **Cluster Manager:** Manages the Hash Ring and node health checks (Heartbeats).



---

## 4. Non-Functional Requirements (NFRs)

| Requirement | Target Metric |
| :--- | :--- |
| **Read Latency** | $< 1ms$ (p99) |
| **Write Latency** | $< 5ms$ (p99) with async replication |
| **Fault Tolerance** | No data loss for single-node failure (Replication Factor $\ge 2$) |
| **Memory Efficiency** | Use off-heap memory (Direct Buffers) to avoid Java GC pauses |

---

## 5. Technical Pitfalls to Avoid
* **The Thundering Herd:** Ensure that when a key expires, multiple clients don't all rush to hit the database at once. Implement "Request Coalescing."
* **Lock Contention:** Avoid synchronizing the entire Map. Use "Striped Locking" or `ConcurrentHashMap` segments.
* **Serialization Overhead:** Avoid standard Java Serialization; use **Protobuf** or **Kryo** for faster, smaller payloads.

---

**Next Steps:**
* Should we dive into the **Low-Level Design (LLD)** for the LRU implementation?
* Would you like to see a **Java code skeleton** for the Phase 1 `CacheStore`?





To elevate your **KeyShard** project from a basic Spring wrapper to a high-performance systems engine, you need to address the "bottleneck of the few"—the global locks that occur during eviction and the overhead of the Java Garbage Collector (GC).

Here is the detailed documentation for the advanced architectural features of a production-grade cache.

---

## 1. Minimal Locking LRU (Concurrent LRU)
**The Problem:** Standard `LinkedHashMap` or a global `List` requires a full lock every time you `GET` a key because the "access order" must be updated (moving the node to the head). This turns your multi-threaded cache into a single-threaded bottleneck.

**The Solution: Scalable-Order Buffering**
Instead of updating the LRU list immediately on every read, we use a **Striped Ring Buffer** or an **MPSC (Multiple Producer Single Consumer) Queue**.

* **Mechanism:** When a thread performs a `GET`, it records the "hit" in a tiny, thread-local buffer. 
* **Batching:** Once the buffer reaches a certain size (e.g., 64 hits), a single background thread drains the buffer and updates the actual LRU pointers in one batch.
* **Result:** 99% of reads require **zero locks** on the LRU structure.



---

## 2. Segmented Cache Architecture
**The Problem:** Even with `ConcurrentHashMap`, a single table can suffer from "Hash Bin Contention" under extreme load (millions of requests per second).

**The Solution: Internal Sharding**
We split the cache into $2^n$ independent segments (shards). Each shard has its own lock, its own LRU list, and its own Expiry Reaper.

* **Key Mapping:** `int segmentIndex = (key.hashCode() & (numSegments - 1));`
* **Benefit:** 16 segments mean 16 threads can write simultaneously without ever seeing each other. This is how `ConcurrentHashMap` (pre-Java 8) and systems like **Hazelcast** achieve massive throughput.

---

## 3. Off-Heap Memory & Zero-Copy Reads
**The Problem:** Large caches in Java lead to "Stop-the-World" GC pauses. If you have 32GB of cached objects, the JVM spends too much time scanning them.

**The Solution: `DirectByteBuffer` & Manual Memory Management**
Store the actual data outside the JVM Heap. The JVM only holds a small pointer (long) to the memory address.

* **Zero-Copy:** When sending data over the network via Netty, we pass the `DirectBuffer` directly to the Network Interface Card (NIC). The CPU doesn't have to copy the data from the "Java Space" to the "Kernel Space."
* **Serialized Storage:** Data is stored as `byte[]` off-heap. This forces you to serialize objects, but prevents GC from ever seeing your cached data.



---

## 4. How Redis Avoids Locks Entirely
**The Philosophy: The Single-Threaded Event Loop**
Many developers are surprised that Redis, the world's fastest KV-store, is largely single-threaded. 

| Feature | Redis Approach | Why it works |
| :--- | :--- | :--- |
| **Concurrency** | **I/O Multiplexing (epoll)** | It handles thousands of connections but processes commands one by one very fast. |
| **No Locks** | No `synchronized` blocks. | Since only one thread modifies the data, there is never a race condition. |
| **Atomic Ops** | Built-in. | Commands like `INCR` or `HSET` are atomic by nature because no other thread can interrupt them. |

**The Lesson for KeyShard:** If you want to build a "Redis-killer" in Java, don't try to out-lock Redis. Instead, use **Affinity Threading**: Assign specific keys to specific threads (Thread Per Core) so that each thread manages its own "shard" of memory with zero locks.

---

## 5. High-Performance `CacheEntry` Structure
To minimize memory footprint, we move away from `Objects` and toward **Primitive Packing**.

```java
// Optimized for memory density
public class CompactCacheEntry {
    // 8 bytes: Address pointer to Off-Heap memory
    private long dataAddress; 
    
    // 4 bytes: Length of the data
    private int dataLength; 
    
    // 8 bytes: Packed metadata (TTL + Version + Flags)
    // Using bitmasking to store multiple values in one long
    private long metadata; 
}
```

### Next Steps Recommendation
If you want to implement one of these now, I suggest the **Segmented Cache**. It is the most "Spring-friendly" way to drastically increase the performance of your `CacheService`. 

**Would you like the code for a Sharded/Segmented CacheService?**


To move away from slow, bulky HTTP/JSON and toward a high-performance system like Redis or Hazelcast, you need a **Binary Wire Format**. This format defines how every byte is laid out so the server can parse requests with zero ambiguity and maximum speed.

By including a **Type ID**, you allow the server to instantly know how to deserialize the payload into your registered domain classes.

---

### 1. The KeyShard Binary Frame Design
A "Frame" is the complete packet sent over TCP. We use a **Length-Prefix** header so the receiver knows exactly how many bytes to read before processing.

#### Packet Structure (The "Wire" Layout)
| Offset (Bytes) | Field | Size | Description |
| :--- | :--- | :--- | :--- |
| 0-3 | **Total Length** | 4 bytes | Length of the entire packet (excluding these 4 bytes). |
| 4 | **Command Code** | 1 byte | 0x01: GET, 0x02: SET, 0x03: DEL, 0x04: EXECUTE. |
| 5-6 | **Type ID** | 2 bytes | The registered Domain ID (e.g., 1 for `User`, 0 for `String`). |
| 7-10 | **TTL** | 4 bytes | Time-to-live in seconds (0 for no expiry). |
| 11-12 | **Key Length** | 2 bytes | Length of the key string. |
| 13-N | **Key** | Variable | The UTF-8 encoded key. |
| N+1-M | **Value/Payload** | Variable | The serialized domain object bytes. |



---

### 2. Implementing the Netty Encoder/Decoder
In Spring Boot, we integrate this logic using **Netty's Codecs**.

#### The Decoder (ByteToMessageDecoder)
This converts raw incoming bytes into a POJO command.

```java
public class KeyShardDecoder extends ReplayingDecoder<Void> {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        int totalLength = in.readInt();
        byte command = in.readByte();
        short typeId = in.readShort();
        int ttl = in.readInt();
        
        short keyLen = in.readShort();
        byte[] keyBytes = new byte[keyLen];
        in.readBytes(keyBytes);
        String key = new String(keyBytes, StandardCharsets.UTF_8);

        // Calculate remaining bytes for value
        int valueLen = totalLength - (1 + 2 + 4 + 2 + keyLen);
        byte[] valueBytes = new byte[valueLen];
        in.readBytes(valueBytes);

        out.add(new CacheCommand(command, typeId, ttl, key, valueBytes));
    }
}
```

---

### 3. Server-Side Deserialization Logic
Once the `CacheCommand` reaches your service, you use the `Type ID` to turn those bytes back into your domain class.

```java
public Object deserialize(short typeId, byte[] data) {
    Class<?> clazz = domainRegistry.getClass(typeId);
    
    if (clazz == String.class) {
        return new String(data, StandardCharsets.UTF_8);
    }
    
    // Use Kryo or Protobuf for registered domain classes
    return serializer.readObject(data, clazz);
}
```

---

### 4. Advanced: Zero-Copy and Endianness
* **Big-Endian:** In networking, we typically use **Network Byte Order** (Big-Endian). Netty’s `readInt()` handles this by default.
* **ByteBuf Pooling:** Instead of creating a `new byte[]` for every request (which causes GC pressure), Netty allows you to use `ByteBuf.slice()`. This points to the original memory buffer without copying the data—this is "Zero-Copy."

---

### 5. Why this wins over JSON/REST
1.  **Size:** A JSON representation of a `User` object might be 500 bytes. In this binary format, it could be 120 bytes.
2.  **Parsing Speed:** A CPU can parse this binary header in a few nanoseconds using simple offsets. JSON parsing requires complex string manipulation and character escaping.
3.  **Type Safety:** Because the `Type ID` is baked into the protocol, the server never has to "guess" what the data is.

### Next Step for KeyShard
You now have the **Core Engine**, the **Spring API**, the **Background Reaper**, and the **Binary Specification**.

**Would you like to see how to implement the `NettyServer` class that ties this binary protocol into your Spring Boot application?**
