    CREATE DATABASE IF NOT EXISTS atmdb;
    USE atmdb;

    CREATE TABLE IF NOT EXISTS accounts (
      account_id INT PRIMARY KEY,
      owner_name VARCHAR(100) NOT NULL,
      type VARCHAR(20) NOT NULL, -- SAVINGS or CURRENT
      balance DECIMAL(15,2) NOT NULL,
      pin_hash VARCHAR(128) DEFAULT NULL
    );

    CREATE TABLE IF NOT EXISTS transactions (
      id INT AUTO_INCREMENT PRIMARY KEY,
      account_id INT NOT NULL,
      timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
      type VARCHAR(50),
      amount DECIMAL(15,2),
      remark VARCHAR(255),
      FOREIGN KEY (account_id) REFERENCES accounts(account_id)
    );

    -- sample data with PIN hashes (PINs: 1001->1234, 1002->2222, 1003->3333)
    INSERT INTO accounts(account_id, owner_name, type, balance, pin_hash) VALUES
    (1001, 'Dhruv Mittal', 'SAVINGS', 5000.00, '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4'),
(1002, 'Ayush Kumar', 'CURRENT', 1500.00, 'edee29f882543b956620b26d0ee0e7e950399b1c4222f5de05e06425b4c995e9'),
(1003, 'Kriti Biswas', 'SAVINGS', 2500.00, '318aee3fed8c9d040d35a7fc1fa776fb31303833aa2de885354ddf3d44d8fb69')
    ON DUPLICATE KEY UPDATE owner_name=VALUES(owner_name);
