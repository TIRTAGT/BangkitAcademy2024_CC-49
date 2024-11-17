import mysql from 'mysql2/promise';
import { Connector } from "@google-cloud/cloud-sql-connector";

const connector = new Connector()
const clientOpts = await connector.getOptions({
	instanceConnectionName: process.env.CLOUD_SQL_CONNECTION_NAME,
	ipType: process.env.CLOUD_SQL_CONNECTION_IP_TYPE
});
const pool = mysql.createPool({
	...clientOpts,
	user: process.env.CLOUD_SQL_DATABASE_USERNAME,
	password: process.env.CLOUD_SQL_DATABASE_PASSWORD,
	database: process.env.CLOUD_SQL_DATABASE_NAME,
})

console.log("Mencoba terhubung ke Cloud SQL...");
const conn = await pool.getConnection();

// Cek apakah koneksi berhasil dengan menjalankan test query
await conn.query("CREATE TABLE IF NOT EXISTS item (id INT AUTO_INCREMENT PRIMARY KEY, nama VARCHAR(255) NOT NULL, author VARCHAR(255) NOT NULL)");
console.log("Terhubung ke Cloud SQL");

// Saat aplikasi ingin ditutup, tutup koneksi database
process.on('SIGINT', async () => {
	conn.release();

	try {
		pool.end().then(() => {
			connector.close();
		});
	}
	catch (_) {}
});

export default conn;