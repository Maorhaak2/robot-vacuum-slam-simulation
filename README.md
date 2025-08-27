# Robot Vacuum Cleaner Simulation – Sensor Fusion & SLAM

## Overview
A concurrent, microservices-based perception & mapping simulator in Java. It models Camera, LiDAR, and GPS/IMU services running in parallel under a tick-driven time service; their streams are fused by a Fusion-SLAM service into a consistent world view and run-time stats. Under the hood, a thread-safe MessageBus exposes an event/broadcast API with Futures for results and round-robin dispatch across workers—services subscribe to message types, emit events, and can spawn new work as they process messages, enabling scalable, decoupled concurrency.

## Features
- Thread-safe **MessageBus** for events & broadcasts.
- **Sensor services**: Camera detections, LiDAR point clouds, GPS/IMU pose.
- **Fusion-SLAM service**: fuses sensor data into a global landmark map.
- **Time service** with discrete ticks for synchronization.
- **Resilience**: sensor crash handling and JSON output snapshot.
- **Output**: `output_file.json` with statistics and final map.

## Usage
**Requirements**: Java 8+ (tested on JDK 23), Maven 3.6+

Compile the project, run the simulation with a configuration file, or execute tests:
```bash
mvn clean compile
mvn exec:java --% -Dexec.mainClass=bgu.spl.mics.application.GurionRockRunner -Dexec.args="example_input/configuration_file.json"
mvn test
