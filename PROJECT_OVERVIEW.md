# MyBank Project - Complete Overview

## Table of Contents
- [Project Information](#project-information)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Data Models](#data-models)
- [Key Features](#key-features)
- [Security](#security)

---

## Project Information

**Project Name:** MyBank  
**Platform:** Android (Jetpack Compose)  
**Language:** Kotlin  
**Backend:** Firebase (Firestore, Authentication, Cloud Functions, Cloud Messaging)  
**Build System:** Gradle (Kotlin DSL)  
**Minimum SDK:** 24 (Android 7.0)  
**Target SDK:** 36 (Android 14)  
**Version:** 1.0

---

## Technology Stack

### Core Android Libraries
- **Jetpack Compose** - Modern UI toolkit
- **AndroidX Lifecycle** - ViewModel, Compose lifecycle
- **Hilt** - Dependency Injection
- **Navigation Compose** - App navigation
- **Room** - Local database (offline-first)
- **DataStore** - Preferences storage
- **WorkManager** - Background tasks
- **Coroutines** - Asynchronous programming

### Firebase Services
- **Firebase Authentication** - User authentication (Email/Password, Google)
- **Cloud Firestore** - NoSQL cloud database
- **Cloud Storage** - File storage
- **Cloud Messaging (FCM)** - Push notifications
- **Cloud Functions** - Server-side logic
- **Firebase Analytics** - App analytics

### Networking & Utilities
- **Retrofit** - HTTP client
- **OkHttp** - HTTP logging
- **Coil** - Image loading
- **Haze** - Glassmorphism blur effects
- **Biometric Authentication** - Fingerprint/Face ID

---

## Architecture

### Pattern: MVVM (Model-View-ViewModel) with Repository Pattern

```
┌─────────────────────────────────────────────────────────────┐
│                         Presentation Layer                     │
│                    (Composables + ViewModels)                │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                         Domain Layer                          │
│                     (Repositories + Models)                  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                          Data Layer                           │
│  ┌──────────────┐         ┌──────────────┐                  │
│  │   Firestore  │         │     Room      │                  │
│  │   (Remote)   │         │   (Local)     │                  │
│  └──────────────┘         └──────────────┘                  │
└─────────────────────────────────────────────────────────────┘
```

### Key Components

#### Presentation Layer
- **Screens:** Composable UI components (HomeScreen, AddMoney, InternalTransfer, etc.)
- **ViewModels:** Manage UI state and business logic
- **Navigation:** Jetpack Navigation Compose
- **UI Components:** Custom components (GlassBottomNavigation, BankCard, etc.)

#### Domain Layer
- **Repositories:** Abstract data access (AccountRepository, TransactionRepository, AuthRepository, etc.)
- **Data Models:** Data classes representing entities (Account, Transaction, User, Card, etc.)
- **FirestoreService:** Firebase-specific operations
- **MyBankApiService:** REST API integration
- **PreferencesManager:** Local preferences storage

#### Data Layer
- **Room Database:** Local offline storage with DAOs
- **Firestore:** Cloud database with real-time updates
- **DataStore:** Encrypted preferences storage

---

## Data Models

### Core Entities

#### Account
```kotlin
@Entity(tableName = "accounts")
data class Account(
    val id: String,
    val userId: String,
    val accountNumber: String,
    val accountName: String,
    val accountType: AccountType,  // CHECKING, SAVINGS, CREDIT, GOAL, etc.
    val balance: Double,
    val currency: String = "USD",
    val iban: String?,
    val createdAt: Long,
    val isActive: Boolean = true,
    val potColorTag: PotColorTag?,
    val goalAmount: Double?,
    val goalDeadline: Long?,
    val tagLabel: String?
)
```

#### Transaction
```kotlin
@Entity(tableName = "transactions")
data class Transaction(
    val id: String,
    val accountId: String,
    val userId: String,
    val type: TransactionType,  // DEBIT, CREDIT
    val category: TransactionCategory,  // TRANSFER, PAYMENT, DEPOSIT, etc.
    val amount: Double,
    val currency: String,
    val description: String,
    val recipientName: String?,
    val recipientAccount: String?,
    val timestamp: Long,
    val status: String,
    val balanceAfter: Double?
)
```

#### User
```kotlin
data class User(
    val id: String,
    val email: String,
    val name: String,
    val phone: String?,
    val profileImageUrl: String?,
    val firebaseUid: String?,
    val passwordHash: String?,  // For offline login
    val createdAt: Long?,
    val lastLoginTimestamp: Long?
)
```

#### Card
```kotlin
@Entity(tableName = "cards")
data class Card(
    val id: String,
    val userId: String,
    val cardNumber: String,
    val cardType: CardType,  // DEBIT, CREDIT, VIRTUAL
    val cardHolderName: String,
    val expiryDate: String,
    val cvv: String,
    val currency: String,
    val balance: Double,
    val cardLimit: Double?,
    val isActive: Boolean,
    val isFrozen: Boolean,
    val createdAt: Long
)
```

---

## Key Features

### 1. Authentication
- Email/Password registration and login
- Google Sign-In integration
- Password reset functionality
- Offline login capability (using Room + hashed passwords)
- Session management with DataStore

### 2. Account Management
- Multiple account types (Checking, Savings, Credit, Goals)
- Account creation and management
- Real-time balance tracking
- Account details view

### 3. Transactions
- Real-time transaction updates via Firestore listeners
- Transaction history and filtering
- Transaction details view
- Balance after transaction tracking

### 4. Transfers
- Internal transfers between user's accounts
- P2P (Peer-to-Peer) transfers to other users
- Send money to contacts
- Recipient search and history
- Atomic transactions with validation

### 5. Money Deposit (Add Money)
- Deposit via saved payment methods
- Add new payment methods
- Card entry for new deposits
- Instant processing

### 6. Cards
- Virtual and physical card management
- Card creation
- Card freezing/unfreezing
- Card details view

### 7. Bill Payments
- Biller management
- Scheduled payments
- Payment history

### 8. Savings Goals
- Create savings goals (Pots)
- Track progress
- Automated savings rules
- Target amounts and deadlines

### 9. Notifications
- Real-time push notifications via FCM
- In-app notification center
- Transaction notifications
- System notifications (low balance, etc.)

### 10. Analytics
- Spending analytics and charts
- Transaction categorization
- Spending trends

### 11. Statements
- Generate bank statements
- Download transaction history
- PDF export support

### 12. Settings & Security
- Profile management
- Two-factor authentication (SMS, Email, Authenticator App)
- Biometric authentication
- Appearance settings
- Change password
- Limits & fees configuration

### 13. KYC (Know Your Customer)
- Identity verification
- Document upload
- Status tracking (PENDING, UNDER_REVIEW, APPROVED, REJECTED)

---

## Security

### Cloud Firestore Rules
Firestore security rules ensure:
- User isolation (only access own data)
- Authentication required for all operations
- Atomic transaction operations
- Read permissions based on ownership

### Key Security Features
1. **Firebase Authentication:** Verified user sessions
2. **Custom Token Claims:** Admin verification
3. **Firestore Security Rules:** Server-side validation
4. **Hashed Passwords:** For offline login (SHA-256)
5. **Biometric Authentication:** Local access protection
6. **2FA Support:** SMS, Email, TOTP

### Atomic Operations
All financial operations use:
- Firestore batch operations for atomicity
- Transaction validation before execution
- Balance checks to prevent overdrafts
- Daily transfer limits
- Fraud detection (large amounts trigger additional checks)

---

## Offline Support

### Strategy: Offline-First
1. **Room Database:** Local cache of all user data
2. **Firestore Listeners:** Real-time sync when online
3. **Offline Login:** Cached credentials in Room
4. **Background Sync:** WorkManager for pending operations

### Data Flow
```
User Action → Local Room Update → Firestore Sync (when online)
           ↓
         UI Update
```

---

## Firebase Cloud Functions

The app uses Firebase Cloud Functions for:
1. **User Creation Triggers:** Initialize new users with accounts
2. **Transaction Triggers:** Create notifications
3. **Push Notifications:** FCM message delivery
4. **Transfers:** Server-side transfer execution with validation
5. **2FA:** Send/verify verification codes
6. **KYC:** Identity verification processing
7. **Account Statements:** Generate and store statements
8. **Scheduled Tasks:** Daily low balance checks

---

## Project Structure

```
app/src/main/java/com/example/mybank/
├── data/
│   ├── firebase/              # Firebase services
│   ├── local/
│   │   ├── dao/              # Room DAOs
│   │   ├── Converters.kt
│   │   ├── MyBankDatabase.kt
│   │   └── Migrations.kt
│   ├── model/                # Data models
│   ├── preferences/          # DataStore preferences
│   ├── remote/               # Retrofit API services
│   ├── manager/              # Notification manager
│   └── repository/           # Repository implementations
├── di/                       # Dependency Injection (Hilt)
├── navigation/               # Navigation graph
├── presentation/
│   ├── home/
│   ├── auth/
│   ├── transfers/
│   ├── send_money/
│   ├── add_money/
│   ├── internal_transfer/
│   ├── cards/
│   ├── bill_payments/
│   ├── savings_goals/
│   ├── notifications/
│   ├── settings/
│   ├── profile/
│   ├── analytics/
│   ├── statements/
│   ├── account_details/
│   ├── transaction_details/
│   └── components/           # Reusable UI components
├── ui/                       # Theme and styling
├── util/                     # Utilities
└── workers/                  # Background workers)

functions/
└── index.js                  # Firebase Cloud Functions

app/
├── build.gradle.kts          # App dependencies
├── google-services.json      # Firebase config
└── proguard-rules.pro        # ProGuard rules

firestore.rules               # Firestore security rules
storage.rules                 # Cloud Storage security rules
```

---

## Next Steps

For detailed analysis of specific functionalities, see:
- [REAL_TIME_UPDATES.md](./REAL_TIME_UPDATES.md) - Real-time balance updates
- [TRANSFERS_ANALYSIS.md](./TRANSFERS_ANALYSIS.md) - Transfer functionality
- [DEPOSIT_ANALYSIS.md](./DEPOSIT_ANALYSIS.md) - Money deposit functionality
- [CLOUD_FUNCTIONS_ANALYSIS.md](./CLOUD_FUNCTIONS_ANALYSIS.md) - Backend logic
- [FINDINGS_RECOMMENDATIONS.md](./FINDINGS_RECOMMENDATIONS.md) - Issues and improvements