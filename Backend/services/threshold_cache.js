const db = require('./db');

// this caches threshold values so we dont have to keep re-querying the threshold table

let thresholdCache = {} //maps (patient_id, sensor_id):[lower, upper]

// queries the database to generate a threhold cache, use this when server restarts (or later if you want to rebuild the table)
function createThresholdCache() {
    async function get() {
        const rows = await db.query(`SELECT * FROM threshold`);
        return rows;
    }

    get().then(rows => {
        for (i = 0; i < rows.length; i++) {
            jsonObject = rows[i]
            thresholdCache[create2DKey(jsonObject.patient_id, jsonObject.sensor_id)] = [jsonObject.lower, jsonObject.upper];
        }
        console.log(thresholdCache)

    }).catch(error => {
        console.error('Error:', error);
    });
}

function create2DKey(key1, key2) {
    return `${key1}_${key2}`;
}

function getThresh(patientID, sensorID) {
    let accessString = create2DKey(patientID, sensorID);
    return thresholdCache[accessString];
}

function addThresh(patientID, sensorID, thresholdLower, thresholdUpper) {
    let accessString = create2DKey(patientID, sensorID);
    thresholdCache[accessString] = [thresholdLower, thresholdUpper];
}

function deleteThresh(patientID, sensorID){
    let accessString = create2DKey(patientID, sensorID);
    delete thresholdCache[accessString];
}

// used for indexing into our cache
function create2DKey(key1, key2) {
    return `${key1}_${key2}`;
}

// returns true if it exists, false if it doesn't
function existsInCache(patientID, sensorID) {
    let accessString = create2DKey(patientID, sensorID);
    return accessString in thresholdCache;
}

module.exports = {createThresholdCache, addThresh, getThresh, deleteThresh, existsInCache}