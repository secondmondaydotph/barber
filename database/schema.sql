-- Barber schema: additive, never drops existing tables or records.
CREATE TABLE IF NOT EXISTS users(
 id BIGSERIAL PRIMARY KEY,name VARCHAR(100) NOT NULL,email VARCHAR(200) NOT NULL UNIQUE CHECK(email=lower(email)),
 phone VARCHAR(30) NOT NULL DEFAULT '',password_hash VARCHAR(300) NOT NULL,
 role VARCHAR(16) NOT NULL CHECK(role IN ('CUSTOMER','BARBER','ADMIN')),active BOOLEAN NOT NULL DEFAULT TRUE,created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS services(id BIGSERIAL PRIMARY KEY,name VARCHAR(100) NOT NULL UNIQUE,minutes INT NOT NULL CHECK(minutes BETWEEN 15 AND 240),price NUMERIC(10,2) NOT NULL CHECK(price>=0),active BOOLEAN NOT NULL DEFAULT TRUE);
CREATE TABLE IF NOT EXISTS addons(id BIGSERIAL PRIMARY KEY,name VARCHAR(100) NOT NULL UNIQUE,minutes INT NOT NULL CHECK(minutes BETWEEN 0 AND 120),price NUMERIC(10,2) NOT NULL CHECK(price>=0),active BOOLEAN NOT NULL DEFAULT TRUE);
CREATE TABLE IF NOT EXISTS weekly_schedules(id BIGSERIAL PRIMARY KEY,barber_id BIGINT NOT NULL REFERENCES users(id),weekday INT NOT NULL CHECK(weekday BETWEEN 1 AND 7),start_time TIME NOT NULL,end_time TIME NOT NULL,active BOOLEAN NOT NULL DEFAULT TRUE,UNIQUE(barber_id,weekday),CHECK(end_time>start_time));
CREATE TABLE IF NOT EXISTS schedule_exceptions(id BIGSERIAL PRIMARY KEY,barber_id BIGINT NOT NULL REFERENCES users(id),day DATE NOT NULL,closed BOOLEAN NOT NULL DEFAULT TRUE,start_time TIME,end_time TIME,UNIQUE(barber_id,day),CHECK(closed OR (start_time IS NOT NULL AND end_time IS NOT NULL AND end_time>start_time)));
CREATE TABLE IF NOT EXISTS bookings(id BIGSERIAL PRIMARY KEY,customer_id BIGINT NOT NULL REFERENCES users(id),barber_id BIGINT NOT NULL REFERENCES users(id),service_id BIGINT NOT NULL REFERENCES services(id),starts_at TIMESTAMP NOT NULL,ends_at TIMESTAMP NOT NULL,total NUMERIC(10,2) NOT NULL CHECK(total>=0),description TEXT NOT NULL,status VARCHAR(20) NOT NULL CHECK(status IN ('CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED')),created_at TIMESTAMPTZ NOT NULL DEFAULT now(),CHECK(ends_at>starts_at));
CREATE INDEX IF NOT EXISTS bookings_barber_time ON bookings(barber_id,starts_at,ends_at);
CREATE INDEX IF NOT EXISTS bookings_customer ON bookings(customer_id,starts_at);
CREATE TABLE IF NOT EXISTS booking_addons(booking_id BIGINT NOT NULL REFERENCES bookings(id),addon_id BIGINT NOT NULL REFERENCES addons(id),PRIMARY KEY(booking_id,addon_id));
CREATE TABLE IF NOT EXISTS booking_history(id BIGSERIAL PRIMARY KEY,booking_id BIGINT NOT NULL REFERENCES bookings(id),actor_id BIGINT NOT NULL REFERENCES users(id),status VARCHAR(20) NOT NULL,created_at TIMESTAMPTZ NOT NULL DEFAULT now());
CREATE TABLE IF NOT EXISTS barber_profiles(
 barber_id BIGINT PRIMARY KEY REFERENCES users(id),bio VARCHAR(800) NOT NULL DEFAULT '',
 specialties VARCHAR(300) NOT NULL DEFAULT '',photo_url VARCHAR(500) NOT NULL DEFAULT ''
);
CREATE TABLE IF NOT EXISTS waitlist_entries(
 id BIGSERIAL PRIMARY KEY,customer_id BIGINT NOT NULL REFERENCES users(id),barber_id BIGINT NOT NULL REFERENCES users(id),
 service_id BIGINT NOT NULL REFERENCES services(id),requested_date DATE NOT NULL,requested_time TIME NOT NULL,
 status VARCHAR(16) NOT NULL DEFAULT 'WAITING' CHECK(status IN ('WAITING','OFFERED','BOOKED','CANCELLED')),
 offered_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
 UNIQUE(customer_id,barber_id,service_id,requested_date,requested_time)
);
CREATE INDEX IF NOT EXISTS waitlist_slot_queue ON waitlist_entries(barber_id,requested_date,requested_time,created_at) WHERE status='WAITING';
CREATE TABLE IF NOT EXISTS waitlist_addons(waitlist_id BIGINT NOT NULL REFERENCES waitlist_entries(id) ON DELETE CASCADE,addon_id BIGINT NOT NULL REFERENCES addons(id),PRIMARY KEY(waitlist_id,addon_id));
CREATE TABLE IF NOT EXISTS notifications(
 id BIGSERIAL PRIMARY KEY,user_id BIGINT NOT NULL REFERENCES users(id),message VARCHAR(500) NOT NULL,
 link VARCHAR(300) NOT NULL DEFAULT '',read_at TIMESTAMPTZ,created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS recurring_plans(
 id BIGSERIAL PRIMARY KEY,customer_id BIGINT NOT NULL REFERENCES users(id),barber_id BIGINT NOT NULL REFERENCES users(id),
 service_id BIGINT NOT NULL REFERENCES services(id),appointment_time TIME NOT NULL,interval_weeks INT NOT NULL CHECK(interval_weeks BETWEEN 1 AND 12),
 occurrences INT NOT NULL CHECK(occurrences BETWEEN 2 AND 12),active BOOLEAN NOT NULL DEFAULT TRUE,created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE IF NOT EXISTS booking_recurring(booking_id BIGINT PRIMARY KEY REFERENCES bookings(id),plan_id BIGINT NOT NULL REFERENCES recurring_plans(id));
INSERT INTO services(name,minutes,price) VALUES('Classic Haircut',30,250),('Haircut and Beard',60,450),('Beard Trim',30,200) ON CONFLICT(name) DO NOTHING;
INSERT INTO addons(name,minutes,price) VALUES('Hair Wash',15,75),('Hot Towel',15,100) ON CONFLICT(name) DO NOTHING;
