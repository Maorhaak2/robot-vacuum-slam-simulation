# Robot Vacuum Cleaner Simulation – Sensor Fusion & SLAM

## Overview
A Java simulation of an **autonomous robot vacuum** performing **Simultaneous Localization and Mapping (SLAM)** via multi-sensor fusion (Camera, LiDAR, GPS, IMU).  
Built with a custom **microservice** architecture, multithreading, and an event-driven MessageBus.

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
