--Se crea la base de datos MUTANTES_MELI
CREATE DATABASE MUTANTES_MELI;

--Se accede a la base de datos MUTANTES_MELI
USE MUTANTES_MELI;

--Se crea la tabla TABLA_MUTANTES para acceder a la informacion de cadenas analizadas
CREATE TABLE IF NOT EXISTS TABLA_MUTANTES (
  ID_PETICION BIGINT   NOT NULL AUTO_INCREMENT COMMENT 'Llave primaria de la tabla',
  ES_MUTANTE  BOOLEAN  NOT NULL COMMENT 'Determina si el ADN es mutante o no 0= humano, 1= mutante',
  CADENA_ADN  LONGTEXT NOT NULL COMMENT 'Cadena de AND analizada',
  PRIMARY KEY (ID_PETICION)
) ENGINE=InnoDB COMMENT='Tabla que almacena la informacion de cadenas analizadas';

--DROP TABLE TABLA_MUTANTES;

--Se valida que la tabla existe en la base de datos
SHOW TABLES;