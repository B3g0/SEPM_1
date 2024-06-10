CREATE SEQUENCE IF NOT EXISTS seq_vehicle;
CREATE SEQUENCE IF NOT EXISTS seq_booking;
CREATE SEQUENCE IF NOT EXISTS seq_bind;

CREATE TABLE IF NOT EXISTS vehicle (
  ID INTEGER DEFAULT seq_vehicle.nextval PRIMARY KEY,
  license VARCHAR(255),
  vehicle VARCHAR(255),
  year INTEGER,
  description VARCHAR (255),
  seats VARCHAR(255),
  registration VARCHAR (10),
  drivetrain VARCHAR(255),
  power VARCHAR(255),
  price INTEGER,
  created TIMESTAMP,
  updated TIMESTAMP,
  isdeleted VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS booking (
  ID INTEGER DEFAULT seq_booking.nextval PRIMARY KEY,
  state VARCHAR(255),
  customer VARCHAR(255),
  paymentmethod VARCHAR(255),
  paymentnumber VARCHAR(255),
  orderstart DATETIME,
  orderend DATETIME,
  totalprice INTEGER,
  created DATETIME,
  billed DATETIME,
  billnumber VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS bind (
  ID INTEGER DEFAULT seq_bind.nextval PRIMARY KEY,
  orderid INTEGER,
  vehicleid INTEGER,
  licensenumber VARCHAR(255),
  licensedate VARCHAR(255)
);