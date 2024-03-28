const jwt = require('jwt-simple');
const secret = "1z98AJf901JZAa";

function verifyJWT(req, res, next) {
  const token = req.headers['x-access-token'] || req.headers['authorization'];

  if (!token) {
    return res.status(401).json({ message: 'No token provided.' });
  }

  try {
    const decoded = jwt.decode(token, secret);
    const currentTime = Math.floor(Date.now() / 1000); // Current time in seconds since epoch

    if (decoded.exp < currentTime) {
      // Token has expired
      return res.status(403).json({ message: 'Token has expired.' });
    }

    req.user = decoded;
    next();
  } catch (error) {
    return res.status(403).json({ message: 'Failed to authenticate token.' });
  }
}

module.exports = verifyJWT;
