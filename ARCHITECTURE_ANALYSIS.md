# Frontend Architecture Analysis (Phase 1)

## Project Overview
**Framework**: React 19.2.4  
**HTTP Client**: Fetch API (native, no Axios)  
**State Management**: React Hooks (useState) - Local component state only  
**Backend API**: RESTful (Spring Boot on localhost:8080)  
**Database**: PostgreSQL (via Docker/local)

---

## Component Hierarchy

```
App (Filter State Management)
├── Sidebar (Filter Buttons)
│   └── onClick → onFilterChange → App.filter → TicketList.filter
└── TicketList (Main Container)
    ├── State: [tickets, showCompose, editingTicket]
    ├── ComposeTicket Modal (Create form)
    ├── EditTicket Modal (Update form)
    └── Table
        └── TicketRow[] (List items with Edit button)
            └── onClick Edit → sets editingTicket → shows EditTicket modal
```

---

## State Management Approach
- **Centralized (Local)**: Filter state in App.js, ticket data in TicketList.js
- **No Redux/Context API**: Direct prop drilling
- **No Persistence**: State lost on page refresh
- **Issue**: State updates are **only local** - database not updated on edit

---

## API Request Structure (ticketService.js)
**Base URL**: `http://localhost:8080/tickets`

### Endpoints Implemented:
- `GET /tickets` → returns array of tickets
- `POST /tickets` → creates ticket (works, persists)
- `PUT /tickets/{id}` → updates ticket (called, but **backend returns data unchanged**)
- `DELETE /tickets/{id}` → deletes ticket (available)

### HTTP Method Used:
- **Fetch API** with manual error handling
- No authentication/headers for roles
- No request interceptors

---

## Ticket Object Structure
```javascript
{
  request: 1,                            // Long - Primary Key (NOT "id")
  issue: "wi-fi",                        // String
  description: "student wifi lags...",   // String
  status: "not solved" | "Open" | "Pending" | "Solved" | "Closed",  // String
  createdat: "2026-02-14T16:09:25...",   // LocalDateTime
  updatedat: "2026-02-14T16:09:25..."    // LocalDateTime
}
```

### Note: Primary Key Field Name Mismatch
- **Backend**: Uses `request` field (from entity `@Id private Long request`)
- **Frontend**: Treats as `ticket.request` (correct)
- Some legacy code checks `ticket.id` (should be `ticket.request`)

---

## Status Values Used
Currently available in form:
- "Open" 
- "Pending"
- "In Progress"
- "Solved"
- "Closed"

Actual data from backend:
- "not solved" (lowercase, from existing tickets)
- "Open"

**Issue**: Status case inconsistency (database stores "not solved", form expects "Open")

---

## Form Submission Logic

### ComposeTicket.js (Create)
1. User fills form (issue, description, status)
2. Click "Create Ticket"
3. `handleSubmit` → calls `createTicket(formData)`
4. `ticketService.createTicket()` → POST request
5. Returns new ticket object
6. `onTicketCreated(newTicket)` callback
7. `TicketList` state updated: prepends new ticket = **visible immediately ✓**
8. **Persistence**: ✓ Saved to PostgreSQL

---

### EditTicket.js (Update)
1. User clicks Edit button on ticket row
2. `setEditingTicket(ticket)` → shows EditTicket modal
3. Form pre-fills with current ticket data
4. User modifies fields (especially status)
5. Click "Update Ticket"
6. `handleSubmit` → calls `updateTicket(ticket.request, formData)`
7. `ticketService.updateTicket()` → PUT request to `/tickets/{request}`
8. Returns updated ticket object
9. `onTicketUpdated(updatedTicket)` callback
10. `TicketList` state updated: maps tickets, replaces matching ID
11. **Visual update**: ✓ UI shows new status immediately
12. **Persistence**: ❌ **Database NOT updated** (see Phase 2)

---

## Current Data Flow

### Loading Tickets (✓ Works)
```
App mounts → TicketList renders → useEffect → getAllTickets() → fetch GET /tickets → setTickets(data)
```

### Creating Ticket (✓ Works End-to-End)
```
ComposeTicket form → handleSubmit → createTicket(POST) → backend saves → returns new ticket → TicketList.setTickets([new, ...old])
```

### Editing Ticket (❌ Partial - Frontend only)
```
EditTicket form → handleSubmit → updateTicket(PUT) → backend IGNORES payload → returns original data → TicketList state updated with old data hidden in form changes
```

---

## What's Missing

### Phase 2 Issues (Persistence):
1. **Backend PUT endpoint** may not be persisting changes to PostgreSQL
2. **EditTicket form** may not be sending data correctly structured for backend
3. **No validation** on what fields can be updated
4. **No timestamps** being updated (updatedat should refresh)

### Phase 3 Issues (RBAC):
1. **No authentication** mechanism (JWT, session, etc.)
2. **No role storage** in frontend
3. **No API headers** for role-based access control
4. **No conditional rendering** for User vs Admin
5. **Sidebar shows all statuses** to all users
6. **No backend authorization** checking user permissions

---

## Technical Dependencies
- **React**: Component-based, hooks-based (useState, useEffect)
- **Fetch API**: Native browser API for HTTP
- **CSS**: Plain CSS files per component (not CSS-in-JS)
- **No build step for auth**: Would need middleware or context provider

---

## Recommended Next Steps

### Phase 2 (Fix Persistence):
1. Debug backend PUT `/tickets/{id}` endpoint (verify it updates DB)
2. Log request/response in EditTicket to confirm data sent correctly
3. Verify TicketEntity setter methods work (setIssue, setDescription, status)
4. Add console logs in backend to trace update flow

### Phase 3 (Add RBAC):
1. Create AuthContext to store user role and permissions
2. Add login/role selection UI
3. Implement role-based filtering in Sidebar (Admin sees all, User sees Open/Closed only)
4. Implement role-based status options in EditTicket (User restricted options)
5. Add authorization headers to API calls
6. Update backend controller with @PreAuthorize or permission checks

---

## Summary Table

| Aspect | Technology | Status |
|--------|-----------|--------|
| HTTP Client | Fetch API | ✓ Works |
| State Management | React Hooks | ✓ Works (local only) |
| Authentication | None | ❌ Missing |
| Authorization (RBAC) | None | ❌ Missing |
| Database Persistence | ✓ Create | ✓ Works |
| Database Persistence | ✓ Read | ✓ Works |
| Database Persistence | ✓ Update | ❌ **BUG** |
| Database Persistence | ✓ Delete | Not tested |
| Filtering | Frontend State | ✓ Works |
| Form Validation | Basic | ✓ Works |
| Error Handling | Try/Catch | ✓ Works |

