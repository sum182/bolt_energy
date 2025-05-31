-- Criação do banco de dados
CREATE DATABASE IF NOT EXISTS bolt_energy_db 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- Define o banco de dados padrão
USE bolt_energy_db;

-- Garante que as permissões sejam aplicadas
FLUSH PRIVILEGES;
