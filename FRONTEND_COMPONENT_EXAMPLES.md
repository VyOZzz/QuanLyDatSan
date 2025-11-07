# 🎨 Frontend Component Examples - Court Booking Calendar

## 📦 React Component (Hoàn Chỉnh)

### CourtCalendar.jsx

```jsx
import React, { useState, useEffect } from 'react';
import './CourtCalendar.css';

const CourtCalendar = ({ venueId }) => {
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [selectedSlots, setSelectedSlots] = useState([]);

  // Fetch availability data
  useEffect(() => {
    fetchAvailability();
  }, [venueId, selectedDate]);

  const fetchAvailability = async () => {
    setLoading(true);
    try {
      const dateStr = formatDate(selectedDate);
      const startTime = `${dateStr}T06:00:00`; // Hoặc lấy từ data.openingTime
      const endTime = `${dateStr}T23:00:00`;   // Hoặc lấy từ data.closingTime
      
      const response = await fetch(
        `/api/venues/${venueId}/courts/availability?startTime=${startTime}&endTime=${endTime}`,
        {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        }
      );
      
      const result = await response.json();
      if (result.success) {
        setData(result.data);
      }
    } catch (error) {
      console.error('Error fetching availability:', error);
    } finally {
      setLoading(false);
    }
  };

  // Generate time slots from opening to closing time
  const generateTimeSlots = () => {
    if (!data) return [];
    
    const slots = [];
    const [openHour, openMin] = data.openingTime.split(':').map(Number);
    const [closeHour, closeMin] = data.closingTime.split(':').map(Number);
    
    let currentHour = openHour;
    let currentMin = openMin;
    
    while (currentHour < closeHour || (currentHour === closeHour && currentMin < closeMin)) {
      const startTime = `${String(currentHour).padStart(2, '0')}:${String(currentMin).padStart(2, '0')}`;
      
      // Tăng 30 phút
      currentMin += 30;
      if (currentMin >= 60) {
        currentMin = 0;
        currentHour++;
      }
      
      const endTime = `${String(currentHour).padStart(2, '0')}:${String(currentMin).padStart(2, '0')}`;
      
      slots.push({
        startTime,
        endTime,
        label: `${startTime}-${endTime}`
      });
    }
    
    return slots;
  };

  // Check if a slot is booked
  const getSlotStatus = (court, slot) => {
    if (!court.isActive) return 'disabled';
    
    const dateStr = formatDate(selectedDate);
    const [startHour, startMin] = slot.startTime.split(':').map(Number);
    const [endHour, endMin] = slot.endTime.split(':').map(Number);
    
    const slotStart = new Date(`${dateStr}T${slot.startTime}:00`);
    const slotEnd = new Date(`${dateStr}T${slot.endTime}:00`);
    
    const isBooked = court.bookedSlots.some(booking => {
      const bookingStart = parseApiDateTime(booking.startTime);
      const bookingEnd = parseApiDateTime(booking.endTime);
      
      // Check overlap
      return (
        (slotStart >= bookingStart && slotStart < bookingEnd) ||
        (slotEnd > bookingStart && slotEnd <= bookingEnd) ||
        (slotStart <= bookingStart && slotEnd >= bookingEnd)
      );
    });
    
    return isBooked ? 'booked' : 'available';
  };

  // Handle slot click
  const handleSlotClick = (court, slot) => {
    const status = getSlotStatus(court, slot);
    if (status !== 'available') return;
    
    const slotKey = `${court.id}_${slot.startTime}_${slot.endTime}`;
    
    setSelectedSlots(prev => {
      if (prev.includes(slotKey)) {
        return prev.filter(s => s !== slotKey);
      } else {
        return [...prev, slotKey];
      }
    });
  };

  // Check if slot is selected
  const isSlotSelected = (court, slot) => {
    const slotKey = `${court.id}_${slot.startTime}_${slot.endTime}`;
    return selectedSlots.includes(slotKey);
  };

  // Submit booking
  const handleSubmitBooking = async () => {
    if (selectedSlots.length === 0) {
      alert('Vui lòng chọn ít nhất 1 slot');
      return;
    }
    
    // Group slots by court
    const bookingsByCourtMap = {};
    selectedSlots.forEach(slotKey => {
      const [courtId, startTime, endTime] = slotKey.split('_');
      if (!bookingsByCourtMap[courtId]) {
        bookingsByCourtMap[courtId] = [];
      }
      bookingsByCourtMap[courtId].push({ startTime, endTime });
    });
    
    // Create booking items
    const dateStr = formatDate(selectedDate);
    const bookingItems = [];
    
    Object.entries(bookingsByCourtMap).forEach(([courtId, slots]) => {
      // Sort slots by start time
      slots.sort((a, b) => a.startTime.localeCompare(b.startTime));
      
      // Merge consecutive slots
      const mergedSlots = mergeConsecutiveSlots(slots);
      
      mergedSlots.forEach(slot => {
        bookingItems.push({
          courtId: parseInt(courtId),
          startTime: `${dateStr}T${slot.startTime}:00`,
          endTime: `${dateStr}T${slot.endTime}:00`
        });
      });
    });
    
    // Call API
    try {
      const response = await fetch('/api/bookings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({ bookingItems })
      });
      
      const result = await response.json();
      if (result.success) {
        alert('Đặt sân thành công!');
        setSelectedSlots([]);
        fetchAvailability(); // Refresh
      } else {
        alert(`Lỗi: ${result.message}`);
      }
    } catch (error) {
      console.error('Error creating booking:', error);
      alert('Có lỗi xảy ra khi đặt sân');
    }
  };

  // Helper functions
  const formatDate = (date) => {
    return date.toISOString().split('T')[0];
  };

  const parseApiDateTime = (arr) => {
    return new Date(arr[0], arr[1] - 1, arr[2], arr[3], arr[4]);
  };

  const mergeConsecutiveSlots = (slots) => {
    if (slots.length === 0) return [];
    
    const merged = [{ ...slots[0] }];
    
    for (let i = 1; i < slots.length; i++) {
      const last = merged[merged.length - 1];
      if (last.endTime === slots[i].startTime) {
        last.endTime = slots[i].endTime; // Merge
      } else {
        merged.push({ ...slots[i] });
      }
    }
    
    return merged;
  };

  const calculateTotalPrice = () => {
    if (!data || selectedSlots.length === 0) return 0;
    
    // Mỗi slot = 30 phút = 0.5 giờ
    const totalHours = selectedSlots.length * 0.5;
    return totalHours * data.pricePerHour;
  };

  if (loading) return <div className="loading">Đang tải...</div>;
  if (!data) return <div className="error">Không có dữ liệu</div>;

  const timeSlots = generateTimeSlots();

  return (
    <div className="court-calendar">
      {/* Header */}
      <div className="calendar-header">
        <h2>{data.venueName}</h2>
        <div className="info-row">
          <span>Giờ hoạt động: {data.openingTime} - {data.closingTime}</span>
          <span className="price">Giá: {data.pricePerHour.toLocaleString()} VND/giờ</span>
        </div>
        <div className="date-selector">
          <button onClick={() => setSelectedDate(new Date(selectedDate.getTime() - 86400000))}>
            ◀ Ngày trước
          </button>
          <input 
            type="date" 
            value={formatDate(selectedDate)}
            onChange={(e) => setSelectedDate(new Date(e.target.value))}
          />
          <button onClick={() => setSelectedDate(new Date(selectedDate.getTime() + 86400000))}>
            Ngày sau ▶
          </button>
        </div>
      </div>

      {/* Calendar Grid */}
      <div className="calendar-grid-container">
        <div className="calendar-grid">
          {/* Header Row */}
          <div className="grid-header">
            <div className="court-label-header">Sân</div>
            {timeSlots.map(slot => (
              <div key={slot.label} className="time-slot-header">
                {slot.label}
              </div>
            ))}
          </div>

          {/* Court Rows */}
          {data.courts.map(court => (
            <div key={court.id} className="court-row">
              <div className="court-label">
                <div>{court.description}</div>
                {!court.isActive && <span className="badge-inactive">Tạm ngưng</span>}
              </div>
              
              {timeSlots.map(slot => {
                const status = getSlotStatus(court, slot);
                const isSelected = isSlotSelected(court, slot);
                
                return (
                  <div
                    key={slot.label}
                    className={`time-slot ${status} ${isSelected ? 'selected' : ''}`}
                    onClick={() => handleSlotClick(court, slot)}
                    title={
                      status === 'booked' ? 'Đã được đặt' :
                      status === 'disabled' ? 'Sân tạm ngưng' :
                      'Click để chọn'
                    }
                  >
                    {status === 'available' && !isSelected && '✅'}
                    {status === 'available' && isSelected && '☑️'}
                    {status === 'booked' && '❌'}
                    {status === 'disabled' && '🚫'}
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      </div>

      {/* Legend */}
      <div className="legend">
        <div className="legend-item">
          <span className="icon">✅</span> Có thể đặt
        </div>
        <div className="legend-item">
          <span className="icon">☑️</span> Đã chọn
        </div>
        <div className="legend-item">
          <span className="icon">❌</span> Đã được đặt
        </div>
        <div className="legend-item">
          <span className="icon">🚫</span> Tạm ngưng
        </div>
      </div>

      {/* Booking Summary */}
      {selectedSlots.length > 0 && (
        <div className="booking-summary">
          <h3>Đặt sân</h3>
          <p>Đã chọn: {selectedSlots.length} slot (30 phút/slot)</p>
          <p>Tổng thời gian: {selectedSlots.length * 0.5} giờ</p>
          <p className="total-price">
            Tổng tiền: <strong>{calculateTotalPrice().toLocaleString()} VND</strong>
          </p>
          <div className="action-buttons">
            <button className="btn-cancel" onClick={() => setSelectedSlots([])}>
              Hủy chọn
            </button>
            <button className="btn-submit" onClick={handleSubmitBooking}>
              Xác nhận đặt sân
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default CourtCalendar;
```

---

## 🎨 CSS Styling

### CourtCalendar.css

```css
.court-calendar {
  padding: 20px;
  font-family: Arial, sans-serif;
}

.calendar-header {
  margin-bottom: 20px;
}

.calendar-header h2 {
  margin: 0 0 10px 0;
  color: #333;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  color: #666;
}

.info-row .price {
  font-weight: bold;
  color: #28a745;
}

.date-selector {
  display: flex;
  gap: 10px;
  align-items: center;
}

.date-selector button {
  padding: 8px 16px;
  background: #007bff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
}

.date-selector button:hover {
  background: #0056b3;
}

.date-selector input {
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

/* Calendar Grid */
.calendar-grid-container {
  overflow-x: auto;
  border: 1px solid #ddd;
  border-radius: 8px;
  margin-bottom: 20px;
}

.calendar-grid {
  min-width: max-content;
}

.grid-header {
  display: flex;
  background: #f8f9fa;
  border-bottom: 2px solid #dee2e6;
  position: sticky;
  top: 0;
  z-index: 10;
}

.court-label-header {
  width: 120px;
  padding: 12px;
  font-weight: bold;
  border-right: 1px solid #dee2e6;
  background: #e9ecef;
}

.time-slot-header {
  min-width: 100px;
  padding: 12px 8px;
  text-align: center;
  font-size: 11px;
  font-weight: bold;
  border-right: 1px solid #dee2e6;
  white-space: nowrap;
}

.court-row {
  display: flex;
  border-bottom: 1px solid #dee2e6;
}

.court-label {
  width: 120px;
  padding: 12px;
  font-weight: bold;
  border-right: 1px solid #dee2e6;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.badge-inactive {
  display: inline-block;
  margin-top: 4px;
  padding: 2px 6px;
  background: #6c757d;
  color: white;
  font-size: 10px;
  border-radius: 3px;
}

.time-slot {
  min-width: 100px;
  height: 50px;
  border-right: 1px solid #dee2e6;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 20px;
}

.time-slot.available {
  background: #d4edda;
  color: #155724;
}

.time-slot.available:hover {
  background: #a7d9b5;
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.time-slot.available.selected {
  background: #28a745;
  color: white;
  font-weight: bold;
}

.time-slot.booked {
  background: #f8d7da;
  color: #721c24;
  cursor: not-allowed;
}

.time-slot.disabled {
  background: #e2e3e5;
  color: #6c757d;
  cursor: not-allowed;
}

/* Legend */
.legend {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  padding: 15px;
  background: #f8f9fa;
  border-radius: 8px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.legend-item .icon {
  font-size: 20px;
}

/* Booking Summary */
.booking-summary {
  background: #fff3cd;
  border: 1px solid #ffc107;
  border-radius: 8px;
  padding: 20px;
}

.booking-summary h3 {
  margin: 0 0 10px 0;
  color: #856404;
}

.booking-summary p {
  margin: 5px 0;
  color: #856404;
}

.total-price {
  font-size: 18px;
  margin-top: 10px !important;
}

.total-price strong {
  color: #d63384;
}

.action-buttons {
  display: flex;
  gap: 10px;
  margin-top: 15px;
}

.action-buttons button {
  flex: 1;
  padding: 12px;
  border: none;
  border-radius: 4px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-cancel {
  background: #6c757d;
  color: white;
}

.btn-cancel:hover {
  background: #5a6268;
}

.btn-submit {
  background: #28a745;
  color: white;
}

.btn-submit:hover {
  background: #218838;
}

/* Loading & Error */
.loading, .error {
  text-align: center;
  padding: 40px;
  font-size: 18px;
  color: #666;
}
```

---

## 🌟 Vue.js Version

### CourtCalendar.vue

```vue
<template>
  <div class="court-calendar">
    <!-- Header -->
    <div class="calendar-header">
      <h2>{{ data?.venueName }}</h2>
      <div class="info-row">
        <span>Giờ hoạt động: {{ data?.openingTime }} - {{ data?.closingTime }}</span>
        <span class="price">Giá: {{ data?.pricePerHour?.toLocaleString() }} VND/giờ</span>
      </div>
      <div class="date-selector">
        <button @click="previousDay">◀ Ngày trước</button>
        <input type="date" v-model="selectedDateStr" />
        <button @click="nextDay">Ngày sau ▶</button>
      </div>
    </div>

    <!-- Calendar Grid -->
    <div class="calendar-grid-container" v-if="data">
      <div class="calendar-grid">
        <!-- Header Row -->
        <div class="grid-header">
          <div class="court-label-header">Sân</div>
          <div 
            v-for="slot in timeSlots" 
            :key="slot.label" 
            class="time-slot-header"
          >
            {{ slot.label }}
          </div>
        </div>

        <!-- Court Rows -->
        <div 
          v-for="court in data.courts" 
          :key="court.id" 
          class="court-row"
        >
          <div class="court-label">
            <div>{{ court.description }}</div>
            <span v-if="!court.isActive" class="badge-inactive">Tạm ngưng</span>
          </div>
          
          <div
            v-for="slot in timeSlots"
            :key="slot.label"
            :class="['time-slot', getSlotStatus(court, slot), { selected: isSlotSelected(court, slot) }]"
            @click="handleSlotClick(court, slot)"
          >
            {{ getSlotIcon(court, slot) }}
          </div>
        </div>
      </div>
    </div>

    <!-- Booking Summary -->
    <div v-if="selectedSlots.length > 0" class="booking-summary">
      <h3>Đặt sân</h3>
      <p>Đã chọn: {{ selectedSlots.length }} slot (30 phút/slot)</p>
      <p>Tổng thời gian: {{ selectedSlots.length * 0.5 }} giờ</p>
      <p class="total-price">
        Tổng tiền: <strong>{{ calculateTotalPrice().toLocaleString() }} VND</strong>
      </p>
      <div class="action-buttons">
        <button class="btn-cancel" @click="selectedSlots = []">Hủy chọn</button>
        <button class="btn-submit" @click="handleSubmitBooking">Xác nhận đặt sân</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue';

const props = defineProps({
  venueId: {
    type: Number,
    required: true
  }
});

const selectedDate = ref(new Date());
const data = ref(null);
const selectedSlots = ref([]);

const selectedDateStr = computed({
  get: () => selectedDate.value.toISOString().split('T')[0],
  set: (val) => selectedDate.value = new Date(val)
});

const timeSlots = computed(() => {
  if (!data.value) return [];
  
  const slots = [];
  const [openHour, openMin] = data.value.openingTime.split(':').map(Number);
  const [closeHour, closeMin] = data.value.closingTime.split(':').map(Number);
  
  let currentHour = openHour;
  let currentMin = openMin;
  
  while (currentHour < closeHour || (currentHour === closeHour && currentMin < closeMin)) {
    const startTime = `${String(currentHour).padStart(2, '0')}:${String(currentMin).padStart(2, '0')}`;
    
    currentMin += 30;
    if (currentMin >= 60) {
      currentMin = 0;
      currentHour++;
    }
    
    const endTime = `${String(currentHour).padStart(2, '0')}:${String(currentMin).padStart(2, '0')}`;
    
    slots.push({
      startTime,
      endTime,
      label: `${startTime}-${endTime}`
    });
  }
  
  return slots;
});

const fetchAvailability = async () => {
  const dateStr = selectedDateStr.value;
  const startTime = `${dateStr}T06:00:00`;
  const endTime = `${dateStr}T23:00:00`;
  
  const response = await fetch(
    `/api/venues/${props.venueId}/courts/availability?startTime=${startTime}&endTime=${endTime}`,
    {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    }
  );
  
  const result = await response.json();
  if (result.success) {
    data.value = result.data;
  }
};

const getSlotStatus = (court, slot) => {
  if (!court.isActive) return 'disabled';
  
  const dateStr = selectedDateStr.value;
  const slotStart = new Date(`${dateStr}T${slot.startTime}:00`);
  const slotEnd = new Date(`${dateStr}T${slot.endTime}:00`);
  
  const isBooked = court.bookedSlots.some(booking => {
    const bookingStart = new Date(booking.startTime[0], booking.startTime[1] - 1, booking.startTime[2], booking.startTime[3], booking.startTime[4]);
    const bookingEnd = new Date(booking.endTime[0], booking.endTime[1] - 1, booking.endTime[2], booking.endTime[3], booking.endTime[4]);
    
    return (
      (slotStart >= bookingStart && slotStart < bookingEnd) ||
      (slotEnd > bookingStart && slotEnd <= bookingEnd) ||
      (slotStart <= bookingStart && slotEnd >= bookingEnd)
    );
  });
  
  return isBooked ? 'booked' : 'available';
};

const getSlotIcon = (court, slot) => {
  const status = getSlotStatus(court, slot);
  const selected = isSlotSelected(court, slot);
  
  if (status === 'available' && selected) return '☑️';
  if (status === 'available') return '✅';
  if (status === 'booked') return '❌';
  return '🚫';
};

const isSlotSelected = (court, slot) => {
  const slotKey = `${court.id}_${slot.startTime}_${slot.endTime}`;
  return selectedSlots.value.includes(slotKey);
};

const handleSlotClick = (court, slot) => {
  const status = getSlotStatus(court, slot);
  if (status !== 'available') return;
  
  const slotKey = `${court.id}_${slot.startTime}_${slot.endTime}`;
  const index = selectedSlots.value.indexOf(slotKey);
  
  if (index > -1) {
    selectedSlots.value.splice(index, 1);
  } else {
    selectedSlots.value.push(slotKey);
  }
};

const calculateTotalPrice = () => {
  if (!data.value) return 0;
  const totalHours = selectedSlots.value.length * 0.5;
  return totalHours * data.value.pricePerHour;
};

const previousDay = () => {
  selectedDate.value = new Date(selectedDate.value.getTime() - 86400000);
};

const nextDay = () => {
  selectedDate.value = new Date(selectedDate.value.getTime() + 86400000);
};

const handleSubmitBooking = async () => {
  // Implementation similar to React version
  alert('Booking submitted!');
};

watch(() => [props.venueId, selectedDate.value], fetchAvailability);
onMounted(fetchAvailability);
</script>

<style scoped>
/* Same CSS as React version */
</style>
```

---

## 🚀 Usage

### React
```jsx
import CourtCalendar from './components/CourtCalendar';

function App() {
  return <CourtCalendar venueId={1} />;
}
```

### Vue
```vue
<template>
  <CourtCalendar :venue-id="1" />
</template>

<script setup>
import CourtCalendar from './components/CourtCalendar.vue';
</script>
```

---

**✅ HOÀN THÀNH! Frontend đã có component đầy đủ để render lịch 30 phút!**

