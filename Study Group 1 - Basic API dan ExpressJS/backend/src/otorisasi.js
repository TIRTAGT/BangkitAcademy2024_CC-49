import Admin from "firebase-admin";

const FirebaseAdminApp = Admin.initializeApp({
	credential: Admin.credential.applicationDefault(), // Akan menggunakan credential dari environment GOOGLE_APPLICATION_CREDENTIALS
});

export default function(request, response, next) {
	// Pastikan ada header Authorization yang diberikan dari client
	if (!request.headers.authorization) {
		response.status(401).send({
			message: 'Hak akses ditolak'
		});

		console.log(`${request.method} ${request.url}: 401 Tidak ada token otorisasi`);
		return;
	}

	// Cek apakah token memiliki 2 part (Bearer <token>)
	const authParts = request.headers.authorization.split(' ');
	if (authParts.length !== 2) {
		response.status(401).send({
			message: 'Format token otorisasi tidak valid'
		});
		
		console.log(`${request.method} ${request.url}: 401 Token otorisasi tidak valid`);
		return;
	}
	const token = authParts[1];

	// Cek di Firebase apakah token valid
	FirebaseAdminApp.auth().verifyIdToken(token)
		.then((decodedToken) => {
			// Simpan data user dari Firebase ke request agar bisa digunakan di rute API
			request.FirebaseUserData = decodedToken;

			// Lanjutkan ke rute API
			next();
		})
		.catch((error) => {
			response.status(401).send({
				message: 'Hak akses ditolak'
			});

			console.log(`${request.method} ${request.url}: 401 Token otorisasi ditolak, alasan: ${error}`);
		});
}