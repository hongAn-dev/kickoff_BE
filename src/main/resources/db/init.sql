CREATE TABLE IF NOT EXISTS users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tasks (
    id SERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL,
    priority VARCHAR(50) NOT NULL,
    due_date DATE,
    due_time TIME,
    user_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE RESTRICT
);

-- Insert dummy data
INSERT INTO users (name, email, role) VALUES 
('Alice Smith', 'alice@example.com', 'Admin'),
('Bob Johnson', 'bob@example.com', 'User');

INSERT INTO tasks (title, description, status, priority, due_date, user_id) VALUES 
('Setup Database', 'Configure PostgreSQL in Docker', 'DONE', 'HIGH', '2026-03-18 10:00:00', 1),
('Implement User API', 'Create CRUD endpoints for Users', 'DOING', 'HIGH', '2026-03-19 15:00:00', 1),
('Design Frontend UI', 'Create mockups using TailwindCSS', 'TODO', 'MEDIUM', '2026-03-20 17:00:00', 2);
