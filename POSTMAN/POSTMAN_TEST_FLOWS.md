# 📊 POSTMAN TEST FLOW DIAGRAMS

## 🎯 Flow 1: User Đặt Sân (Complete Booking Flow)

```
┌─────────────────────────────────────────────────────────────────┐
│                    USER BOOKING FLOW                             │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐
│ 1. Register  │  POST /api/auth/register
│   User       │  Body: {name, phone, password, email}
└──────┬───────┘  Response: {success: true}
       │
       ▼
┌──────────────┐
│ 2. Login     │  POST /api/auth/login
│   User       │  Body: {phone, password}
└──────┬───────┘  Response: {token, roles: ["ROLE_USER"]}
       │          ✅ Auto-save token
       ▼
┌──────────────┐
│ 3. Get       │  GET /api/users/me
│   User Info  │  Headers: Authorization: Bearer {{token}}
└──────┬───────┘  Response: {id, name, phone, roles}
       │
       ▼
┌──────────────┐
│ 4. Search    │  GET /api/venues/search?province=Hanoi
│   Venues     │  No auth required
└──────┬───────┘  Response: [{id, name, address, openTime...}]
       │          ✅ Auto-save venueId
       ▼
┌──────────────┐
│ 5. Get       │  GET /api/venues/{{venueId}}
│   Venue      │  No auth required
│   Details    │  Response: {id, name, courts[], priceRules[]}
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ 6. Get       │  GET /api/courts
│   Courts     │  No auth required
└──────┬───────┘  Response: [{id, name, courtType, pricePerHour}]
       │          ✅ Auto-save courtId
       ▼
┌──────────────┐
│ 7. Check     │  GET /api/courts/{{courtId}}/availability
│   Available  │      ?startTime=2025-11-01T10:00:00
└──────┬───────┘      &endTime=2025-11-01T12:00:00
       │          Response: {available: true, bookedSlots: []}
       ▼
┌──────────────┐
│ 8. Create    │  POST /api/bookings
│   Booking    │  Headers: Authorization: Bearer {{token}}
└──────┬───────┘  Body: {venueId, bookingItems: [{courtId, startTime, endTime}]}
       │          Response: {id, totalAmount, status: "PENDING"}
       │          ✅ Auto-save bookingId
       ▼
┌──────────────┐
│ 9. Upload    │  PUT /api/bookings/{{bookingId}}/confirm-payment
│   Payment    │  Body: {paymentProofUrl, note}
│   Proof      │  Response: {status: "PAYMENT_CONFIRMED"}
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ 10. Wait     │  Status: PAYMENT_CONFIRMED
│    Owner     │  ⏳ Waiting for owner to accept/reject
│    Confirm   │
└──────┬───────┘
       │
       ├─────────────┬─────────────┐
       ▼             ▼             ▼
   CONFIRMED     REJECTED      CANCELLED
   (Owner OK)   (Owner No)   (User Cancel)
       │
       ▼
┌──────────────┐
│ 11. After    │  POST /api/bookings/{{bookingId}}/review
│    Completed │  Body: {rating: 5, comment: "Great!"}
│    → Review  │  Response: {id, rating, comment}
└──────────────┘
```

---

## 🏢 Flow 2: Owner Quản Lý Sân

```
┌─────────────────────────────────────────────────────────────────┐
│                    OWNER MANAGEMENT FLOW                         │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐
│ 1. Register  │  POST /api/auth/register
│   Owner      │  Body: {name, phone, password, email}
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ 2. Login     │  POST /api/auth/login
│   Owner      │  Response: {token, roles: ["ROLE_USER"]}
└──────┬───────┘  ✅ Auto-save token
       │
       ▼
┌──────────────┐
│ 3. Request   │  POST /api/users/me/request-owner-role
│   Owner      │  Headers: Authorization: Bearer {{token}}
│   Role       │  Response: {message: "Upgraded to OWNER"}
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ 4. Login     │  POST /api/auth/login (Again!)
│   Again      │  ⚠️ Phải login lại để có token mới
└──────┬───────┘  Response: {token, roles: ["ROLE_USER", "ROLE_OWNER"]}
       │          ✅ Token mới có OWNER role
       ▼
┌──────────────┐
│ 5. Update    │  PUT /api/users/me
│   Bank Info  │  Body: {bankName, bankAccountNumber, bankAccountName}
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ 6. Create    │  POST /api/venues
│   Venue      │  Headers: Authorization: Bearer {{token}}
└──────┬───────┘  Body: {name, description, address, openTime, closeTime}
       │          Response: {id, name, ownerId}
       │          ✅ Auto-save venueId
       ▼
┌──────────────┐
│ 7. Create    │  POST /api/courts
│   Court 1    │  Body: {venueId, name: "Sân 1", courtType: "FOOTBALL_5"}
└──────┬───────┘  ✅ Auto-save courtId
       │
       ▼
┌──────────────┐
│ 8. Create    │  POST /api/courts
│   Court 2    │  Body: {venueId, name: "Sân 2", courtType: "FOOTBALL_5"}
└──────┬───────┘
       │
       ▼
┌──────────────┐
│ 9. Create    │  POST /api/pricerules
│   Price Rule │  Body: {venueId, name: "Giờ vàng", 
│   Morning    │         startTime: "06:00", endTime: "09:00",
└──────┬───────┘         pricePerHour: 250000}
       │
       ▼
┌──────────────┐
│ 10. Create   │  POST /api/pricerules
│    Price Rule│  Body: {venueId, name: "Giờ chiều",
│    Evening   │         startTime: "17:00", endTime: "22:00",
└──────┬───────┘         pricePerHour: 400000}
       │
       ▼
┌──────────────────────────────────────────────┐
│        OWNER READY - SÂN ĐÃ SẴN SÀNG!        │
│   Users có thể tìm và đặt sân của Owner      │
└──────────────────────────────────────────────┘
```

---

## 🔔 Flow 3: Owner Xử Lý Booking

```
┌─────────────────────────────────────────────────────────────────┐
│              OWNER HANDLE BOOKING FLOW                           │
└─────────────────────────────────────────────────────────────────┘

           📱 OWNER RECEIVES NOTIFICATION
                      │
                      ▼
           ┌──────────────────────┐
           │  New Booking Alert   │
           │  Status: PENDING     │
           └──────────┬───────────┘
                      │
                      ▼
           ┌──────────────────────┐
           │ 1. Get Pending       │  GET /api/bookings/pending
           │    Bookings          │  Headers: Auth Bearer {{token}}
           └──────────┬───────────┘  Response: [{id, user, court, time, amount}]
                      │
                      ▼
           ┌──────────────────────┐
           │ 2. Get Booking       │  GET /api/bookings/{{bookingId}}
           │    Details           │  Response: Full booking info + payment proof
           └──────────┬───────────┘
                      │
                      ▼
           ┌──────────────────────┐
           │ 3. View Payment      │  GET /api/files/payment-proofs/{filename}
           │    Proof Image       │  Browser opens image
           └──────────┬───────────┘
                      │
                      ▼
           ┌──────────────────────┐
           │  4. DECISION TIME    │
           └──────────┬───────────┘
                      │
        ┌─────────────┴─────────────┐
        ▼                           ▼
┌───────────────┐          ┌────────────────┐
│ 5a. ACCEPT    │          │ 5b. REJECT     │
│                │          │                 │
│ PUT /bookings │          │ PUT /bookings  │
│ /{id}/accept  │          │ /{id}/reject   │
│                │          │ Body: {reason} │
└───────┬───────┘          └────────┬───────┘
        │                           │
        ▼                           ▼
Status: CONFIRMED          Status: REJECTED
User notified ✅          User notified + reason 📧
        │
        ▼
┌───────────────┐
│ 6. Get All    │  GET /api/bookings/venue/{{venueId}}
│    Bookings   │  Response: All bookings of this venue
│    of Venue   │           (PENDING, CONFIRMED, COMPLETED, etc.)
└───────────────┘
```

---

## 🔄 Flow 4: Booking Status Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│                 BOOKING STATUS LIFECYCLE                         │
└─────────────────────────────────────────────────────────────────┘


    CREATE BOOKING
         │
         ▼
    ┌─────────┐
    │ PENDING │  ← User just created booking
    └────┬────┘    Need to transfer money within 5 minutes
         │
         │ User uploads payment proof
         ▼
  ┌──────────────────┐
  │ PAYMENT_CONFIRMED│  ← Waiting for owner
  └────┬────┬────────┘
       │    │
       │    │ Owner rejects
       │    └───────────────────┐
       │                        ▼
       │                   ┌─────────┐
       │                   │REJECTED │  ❌ End state
       │                   └─────────┘
       │
       │ Owner accepts
       ▼
  ┌───────────┐
  │ CONFIRMED │  ← Booking confirmed
  └─────┬─────┘    User can use the court
        │
        │ After end time
        ▼
  ┌───────────┐
  │ COMPLETED │  ✅ End state
  └─────┬─────┘    Now user can review
        │
        ▼
   USER REVIEWS
   (Optional)


  ⚠️ CANCELLED State:
  ┌─────────┐
  │CANCELLED│  User cancels anytime before CONFIRMED
  └─────────┘  ❌ End state
```

---

## 🔐 Authentication & Authorization Flow

```
┌─────────────────────────────────────────────────────────────────┐
│              AUTHENTICATION & AUTHORIZATION                      │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐
│   REGISTER   │
└──────┬───────┘
       │
       ▼
┌──────────────┐
│    LOGIN     │  → JWT Token
└──────┬───────┘    {
       │              "sub": "0123456789",
       │              "roles": ["ROLE_USER"],
       │              "exp": 1635724800
       │            }
       ▼
┌──────────────┐
│  ROLE: USER  │
└──────┬───────┘
       │
       │ Can access:
       ├─→ ✅ GET /api/venues (public)
       ├─→ ✅ POST /api/bookings
       ├─→ ✅ GET /api/bookings/my-bookings
       ├─→ ✅ PUT /api/bookings/{id}/confirm-payment
       ├─→ ✅ POST /api/bookings/{id}/review
       ├─→ ❌ POST /api/venues (OWNER only)
       ├─→ ❌ GET /api/bookings/pending (OWNER only)
       └─→ ❌ PUT /api/bookings/{id}/accept (OWNER only)
       │
       │ Request OWNER role
       ▼
┌──────────────┐
│ POST /users/ │
│ me/request-  │
│ owner-role   │
└──────┬───────┘
       │
       ▼ Login again
┌──────────────┐
│    LOGIN     │  → New JWT Token
└──────┬───────┘    {
       │              "sub": "0123456789",
       │              "roles": ["ROLE_USER", "ROLE_OWNER"],
       │              "exp": 1635724800
       │            }
       ▼
┌──────────────┐
│ROLE: USER +  │
│     OWNER    │
└──────┬───────┘
       │
       │ Can access:
       ├─→ ✅ All USER endpoints
       ├─→ ✅ POST /api/venues
       ├─→ ✅ PUT /api/venues/{id}
       ├─→ ✅ POST /api/courts
       ├─→ ✅ POST /api/pricerules
       ├─→ ✅ GET /api/bookings/pending
       ├─→ ✅ PUT /api/bookings/{id}/accept
       └─→ ✅ PUT /api/bookings/{id}/reject
```

---

## 📊 Data Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                      DATA RELATIONSHIPS                          │
└─────────────────────────────────────────────────────────────────┘

                    ┌──────────┐
                    │   User   │
                    │          │
                    │ - phone  │
                    │ - name   │
                    │ - roles  │
                    └────┬─────┘
                         │
           ┌─────────────┴─────────────┐
           │                           │
    (owner)│                           │(booker)
           ▼                           ▼
      ┌─────────┐                 ┌─────────┐
      │ Venues  │                 │ Booking │
      │         │                 │         │
      │ - name  │                 │ - date  │
      │ - addr  │◄────┐           │ - total │
      └────┬────┘     │           └────┬────┘
           │          │                │
           │          │                │
           ▼          │                │
      ┌─────────┐     │                │
      │ Courts  │     │                │
      │         │     │                │
      │ - name  │─────┘                │
      │ - type  │                      │
      └────┬────┘                      │
           │                           │
           └───────────────────────────┘
                      │
                      ▼
               ┌──────────────┐
               │ BookingItem  │
               │              │
               │ - courtId    │
               │ - startTime  │
               │ - endTime    │
               └──────────────┘

      ┌─────────────┐          ┌──────────────┐
      │ PriceRules  │          │    Review    │
      │             │          │              │
      │ - startTime │          │ - rating     │
      │ - endTime   │          │ - comment    │
      │ - price     │          └──────────────┘
      └─────────────┘

      ┌──────────────┐
      │ Notification │
      │              │
      │ - type       │
      │ - message    │
      │ - read       │
      └──────────────┘
```

---

## 🧪 Test Coverage Map

```
┌─────────────────────────────────────────────────────────────────┐
│                      TEST COVERAGE                               │
└─────────────────────────────────────────────────────────────────┘

MODULE                    APIs    Coverage
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
1. Authentication         6/6     ████████████ 100%
   - Register
   - Login
   - Forgot/Reset Password

2. User Management        3/3     ████████████ 100%
   - Get/Update User
   - Request OWNER role

3. Venues                 6/6     ████████████ 100%
   - CRUD + Search

4. Courts                 6/6     ████████████ 100%
   - CRUD + Availability

5. Price Rules            4/4     ████████████ 100%
   - CRUD

6. Bookings              9/9     ████████████ 100%
   - Create, Confirm
   - Accept, Reject
   - Cancel

7. Reviews               3/3     ████████████ 100%
   - Create, Get

8. Notifications         5/5     ████████████ 100%
   - Get, Read, Delete

9. Files                 1/1     ████████████ 100%
   - Get payment proof

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
TOTAL                    43/43    ████████████ 100%
```

---

## 📱 Mobile App Integration Flow

```
┌─────────────────────────────────────────────────────────────────┐
│            MOBILE APP → BACKEND INTEGRATION                      │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐
│ MOBILE APP   │
│ (Kotlin)     │
└──────┬───────┘
       │
       │ 1. User opens app
       ▼
┌──────────────┐
│ Check Token  │
│ in Storage   │
└──────┬───────┘
       │
   ┌───┴───┐
   │Token? │
   └───┬───┘
       │
  ┌────┴─────┐
  │          │
No│          │Yes
  ▼          ▼
Show      Validate
Login     Token
Screen    
  │          │
  │      ┌───┴────┐
  │      │Valid?  │
  │      └───┬────┘
  │          │
  │     ┌────┴─────┐
  │     │          │
  │   No│          │Yes
  │     ▼          ▼
  │   Show       Load
  │   Login      Home
  │   Screen     Screen
  │     │
  │     ▼
  └───► POST /api/auth/login
        {phone, password}
            │
            ▼
        Save Token
        to Storage
            │
            ▼
     All API calls use:
     Headers: {
       "Authorization": "Bearer {token}"
     }
```

---

## 🎯 Testing Strategy

```
┌─────────────────────────────────────────────────────────────────┐
│                    TESTING PYRAMID                               │
└─────────────────────────────────────────────────────────────────┘

                    ▲
                   ╱ ╲
                  ╱   ╲
                 ╱  E2E ╲           ← Postman Collection
                ╱  Tests ╲            (Full user flows)
               ╱─────────╲
              ╱           ╲
             ╱ Integration╲         ← API Tests
            ╱    Tests     ╲          (Controller → Service → Repository)
           ╱───────────────╲
          ╱                 ╲
         ╱   Unit Tests      ╲      ← JUnit Tests
        ╱  (Service, Utils)   ╲       (Individual methods)
       ╱_______________________╲
      
      Many ←── Quantity ──→ Few
      Fast ←── Speed   ──→ Slow
      Cheap ←── Cost   ──→ Expensive
```

---

## 🚀 Quick Reference

### Key Endpoints
```
Auth:     POST   /api/auth/login
Users:    GET    /api/users/me
Venues:   GET    /api/venues/search
Courts:   GET    /api/courts/{id}/availability
Booking:  POST   /api/bookings
Accept:   PUT    /api/bookings/{id}/accept
Review:   POST   /api/bookings/{id}/review
Notify:   GET    /api/notifications
```

### Variables
```
{{baseUrl}}     = http://localhost:8080
{{token}}       = Auto-saved after login
{{venueId}}     = Auto-saved after create/get venue
{{courtId}}     = Auto-saved after create/get court
{{bookingId}}   = Auto-saved after create booking
```

### Roles
```
PUBLIC  → No auth required
USER    → Can book courts, write reviews
OWNER   → Can manage venues, accept/reject bookings
```

---

**📚 For detailed guide, see:**
- [`QUICK_START_POSTMAN.md`](QUICK_START_POSTMAN.md) - Quick start
- [`HUONG_DAN_TEST_POSTMAN.md`](HUONG_DAN_TEST_POSTMAN.md) - Full guide
- [`README_POSTMAN_TESTING.md`](./README_POSTMAN_TESTING.md) - Overview

**🚀 Happy Testing!**

