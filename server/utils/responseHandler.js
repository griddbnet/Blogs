// Response handler to handle all api responses
exports.responseHandler = (
  res,
  message,
  statusCode,
  success = false,
  data = {}
) => {
  res.status(statusCode).json({
    success,
    message,
    data,
  });
};
