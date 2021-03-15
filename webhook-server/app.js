const crypto = require('crypto');
const createError = require('http-errors');
const express = require('express');
const logger = require('morgan');
const safeCompare = require('safe-compare');

const app = express();

app.use(logger('dev'));
app.use(express.json());

const webhookSecret = process.env.MVNMON_WEBHOOK_SECRET;

function verifySignature(signature, payload) {
  const hash = crypto
    .createHmac('sha256', webhookSecret)
    .update(payload)
    .digest('hex');
  return safeCompare(signature, `sha256=${hash}`);
}

app.post('/webhooks', (req, res) => {
  if (verifySignature(req.header('X-Hub-Signature-256'), JSON.stringify(req.body))) {
    res.status(204);
  } else {
    res.status(403);
  }
  res.send();
});

// catch 404 and forward to error handler
app.use((req, res, next) => {
  next(createError(404));
});

// error handler
app.use((err, req, res) => {
  // set locals, only providing error in development
  res.locals.message = err.message;
  res.locals.error = req.app.get('env') === 'development' ? err : {};

  // render the error page
  res.status(err.status || 500);
  res.render('error');
});

module.exports = app;
