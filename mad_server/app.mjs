import express from 'express';
import Database from 'better-sqlite3';

const app = express();

app.use(express.json());
app.use(express.urlencoded({extended: false}));

const db = new Database("pointsofinterest_mad.db");


// Retrieve all items
app.get('/poi/all', (req, res) => {
    try {
        const stmt = db.prepare("SELECT * FROM pointsofinterest ORDER BY osm_id");
        const mapped = stmt.all().map ( poi => {
			return {
				id: poi.osm_id || poi.local_id,
				lat: poi.lat,
				lon: poi.lon,
                name: poi.name,
				description: poi.description,
				type: poi.type
			};
		});
        res.json(mapped);
    } catch (e) {
        res.status(500).json({error: e});
    }
});

// Create an item
//
// The item data should be passed as POST data.
// If the JSON sent to the server has no ID, the next available ID will
// be allocated.
// If the JSON sent to the server contains an ID, it will be checked. If
// that ID exists already, the item will not be added. If it does
// not exist, the item will be added with that ID.
//
// Responds with a JSON object containing the allocated ID.
app.post('/poi/create', (req, res) => {
    try {
        const { name, type, description, lat, lon } = req.body;
        const stmt = db.prepare("INSERT INTO pointsofinterest(name,type,description,lat,lon) VALUES (?,?,?,?,?)");
        const info = stmt.run(name, type, description, lat, lon);
        res.send(`${info.lastInsertRowid}`);
    } catch(e) {
        res.status(500).json({error: e});
    }
});

const PORT = 3000;

app.listen(PORT, () => {
    console.log(`App listening on port ${PORT}.`);
});
