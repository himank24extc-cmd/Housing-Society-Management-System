# Housing Society Management System

A console-based **Housing Society Management System** developed in Java.

The application provides basic management features for residents, maintenance bills, payments, complaints, notices, and visitors. Data is automatically saved locally so that it can be loaded again when the application is restarted.

## Features

* Add residents
* List all residents
* Generate monthly maintenance bills
* Record bill payments
* View outstanding dues
* View bill history for a flat
* Raise complaints
* Resolve complaints
* List complaints
* Post and view society notices
* Log visitor entry
* Log visitor exit
* View visitor log
* Automatic local data persistence

## Technologies Used

* Java
* Java 11 or later
* Java Serialization
* Java Collections
* `java.time` API
* Console / command-line interface

## Project Structure

```text
Society-Management-System/
│
├── SocietyManager.java
├── README.md
├── .gitignore
└── LICENSE
```

## Requirements

* Java Development Kit (JDK) 11 or later
* Git

## How to Run

### 1. Clone the repository

```bash
git clone https://github.com/YOUR-USERNAME/Society-Management-System.git
```

### 2. Enter the project directory

```bash
cd Society-Management-System
```

### 3. Compile the program

```bash
javac SocietyManager.java
```

### 4. Run the program

```bash
java SocietyManager
```

## Main Menu

When the program starts, the following options are available:

```text
===== HOUSING SOCIETY MANAGEMENT =====
 1. Add resident
 2. List residents
 3. Generate bills
 4. Pay bill
 5. View dues
 6. Flat bill history
 7. Raise complaint
 8. Resolve complaint
 9. List complaints
10. Notices
11. Visitor log
 0. Exit
```

## Data Persistence

The application stores its data in a local file named:

```text
society.dat
```

This file is created automatically when the application saves its data.

The following information is persisted:

* Residents
* Bills
* Complaints
* Notices
* Visitor records
* Bill and complaint ID counters

The `society.dat` file is intentionally excluded from Git because it is runtime-generated data.

## Example

A typical workflow can be:

1. Add residents.
2. Generate monthly maintenance bills.
3. View outstanding dues.
4. Record payments.
5. Raise and resolve complaints.
6. Post society notices.
7. Record visitor entries and exits.
8. Exit the application to save the data.

## Author

Developed as a Java console application for managing common housing society operations.
