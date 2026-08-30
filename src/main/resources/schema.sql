DROP TABLE IF EXISTS transacciones_procesadas;
DROP TABLE IF EXISTS intereses_procesados;
DROP TABLE IF EXISTS cuentas_anuales_procesadas;

CREATE TABLE transacciones_procesadas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    fecha DATE,
    monto DECIMAL(15,2),
    tipo VARCHAR(50),
    descripcion VARCHAR(255),
    estado VARCHAR(100)
);

CREATE TABLE intereses_procesados (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id BIGINT NOT NULL,
    nombre VARCHAR(150),
    saldo DECIMAL(15,2),
    edad INT,
    tipo VARCHAR(50),
    interes DECIMAL(15,2),
    saldo_final DECIMAL(15,2),
    estado VARCHAR(100)
);

CREATE TABLE cuentas_anuales_procesadas (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    cuenta_id BIGINT NOT NULL, 
    fecha DATE,
    monto DECIMAL(15,2),
    tipo VARCHAR(50),
    estado VARCHAR(100)
);