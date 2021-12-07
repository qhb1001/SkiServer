CREATE DATABASE IF NOT EXISTS SkierMicroService;
USE SkierMicroService;

DROP TABLE IF EXISTS LiftRides;

CREATE TABLE LiftRides (
    id INTEGER AUTO_INCREMENT,
    skierId INTEGER,
    liftId INTEGER,
    seasonId INTEGER,
    dayId INTEGER,
    vertical INTEGER,
    PRIMARY KEY (id)
);

CREATE DATABASE IF NOT EXISTS ResortMicroService;
USE ResortMicroService;

DROP TABLE IF EXISTS LiftRides;

CREATE TABLE LiftRides (
    id INTEGER AUTO_INCREMENT,
    resortId INTEGER,
    skierId INTEGER,
    liftId INTEGER,
    seasonId INTEGER,
    dayId INTEGER,
    time INTEGER,
    vertical INTEGER,
    PRIMARY KEY (id),
    INDEX resortId_index (resortId),
    INDEX skierId_index (skierId),
    INDEX liftId_index (liftId),
    INDEX seasonId_index (seasonId),
    INDEX dayId_index (dayId)
);