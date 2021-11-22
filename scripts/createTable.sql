CREATE DATABASE IF NOT EXISTS SkierMicroService;
USE SkierMicroService;

DROP TABLE IF EXISTS LiftRides;

CREATE TABLE LiftRides (
    id INTEGER AUTO_INCREMENT,
    skierId INTEGER,
    liftId INTEGER,
    seasonId VARCHAR(255),
    day INTEGER,
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
    day INTEGER,
    time INTEGER,
    vertical INTEGER,
    PRIMARY KEY (id)
);