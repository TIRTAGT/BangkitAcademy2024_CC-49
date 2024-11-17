import express from 'express';
import validasiToken from './otorisasi.js';
import CloudSQL from './cloud_sql.js';

const app = express();

// Sebelum request di proses lebih lanjut, validasi token dulu
app.use(validasiToken);

// Otomatis read/parse/decode request data JSON
app.use(express.json());

app.get("/", (req, res) => {
	res.send("Selamat datang di API Study Group 1 - Basic API dan ExpressJS with Matthew");
	console.log("GET /");
});

app.get("/item", async(req, res) => {
	let result = await CloudSQL.query("SELECT * FROM item");

	if (result.length !== 2) {
		res.status(500).send(JSON.stringify({ message: 'Terjadi kesalahan pada server' }));
		console.log("GET /item: 500 Terjadi kesalahan pada server");
		return;
	}
	result = result[0];

	res.send(JSON.stringify({ data: result }));
	console.log(`GET /item: ${result.length} total data`);
});

app.post("/item", async(req, res) => {
	let nama = req.body.nama;
	if (!nama) {
		res.status(400).send(JSON.stringify({ message: 'Nama tidak boleh kosong' }));
		console.log("POST /item: 400 Nama tidak boleh kosong");
		return;
	}

	if (!req.FirebaseUserData) {
		res.status(401).send("Sistem token otorisasi tidak berjalan dengan baik");
		console.log("POST /item: 401 Hak akses ditolak");
		return;
	}

	// Insert dan cek apakah berhasil
	let result = await CloudSQL.query("INSERT INTO item (nama, author) VALUES (?, ?)", [nama, req.FirebaseUserData.email || "Unverified Mail User"]);
	if (result.length !== 2) {
		res.status(500).send(JSON.stringify({ message: 'Terjadi kesalahan pada server' }));
		console.log("POST /item: 500 Terjadi kesalahan pada server");
		return;
	}
	result = result[0];

	res.send(JSON.stringify({ message: 'Data berhasil ditambahkan, ID: ' + result.insertId }));
	console.log(`POST /item: ID ${result.insertId} - ${nama}`);
});

app.delete("/item/:id", async(req, res) => {
	const id = req.params.id;

	// Pastikan ID yang diberikan adalah angka
	if (isNaN(id) || id <= 0) {
		res.status(400).send(JSON.stringify({ message: 'ID harus berupa angka positif' }));
		console.log("DELETE /item: 400 ID harus berupa angka positif");
		return
	}

	// Delete dan cek apakah berhasil
	let result = await CloudSQL.query("DELETE FROM item WHERE id = ?", [id]);
	if (result.length !== 2) {
		res.status(500).send(JSON.stringify({ message: 'Terjadi kesalahan pada server' }));
		console.log("DELETE /item: 500 Terjadi kesalahan pada server");
		return;
	}
	result = result[0];

	// Pastikan ada sesuatu yang dihapus (baris terpengaruh)
	if (result.affectedRows === 0) {
		res.status(404).send(JSON.stringify({ message: 'Data tidak ditemukan' }));
		console.log(`DELETE /item: ID ${id} tidak ditemukan`);
		return;
	}

	res.send('Data berhasil dihapus');
	console.log(`DELETE /item: ID ${id}`);
});

// Middleware untuk menangani rute yang tidak ditemukan
app.use((req, res) => {
	res.status(404).send(JSON.stringify({ message: 'Endpoint tidak ditemukan' }));
	console.log("Endpoint tidak ditemukan: ", req.url);
});

app.listen(process.env.EXPRESS_PORT, () => {
	console.log(`Server API menerima request di PORT :${process.env.EXPRESS_PORT}`);
});