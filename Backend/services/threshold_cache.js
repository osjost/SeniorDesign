const db = require('./db');

// this caches threshold values so we dont have to keep re-querying the threshold table

let thresholdCache = {} //maps id:[lower, upper]

// queries the database to generate a threhold cache, use this when server restarts (or later if you want to rebuild the table)
function createThresholdCache() {
    async function get(patientId, sensorId) {
        const rows = await db.query(
            `SELECT * FROM threshold`,
            [patientId, sensorId]
        );
    }

}

function getThresh(patientID) {
    return thresholdCache.patientID

}

function addThresh(patientID, thresholdLower, thresholdUpper) {
    thresholdCache.patientID = [thresholdLower, thresholdUpper]
}

function deleteThresh(patientID){
    delete thresholdCache.patientID

}

module.exports = {createThresholdCache, addThresh, getThresh, deleteThresh}