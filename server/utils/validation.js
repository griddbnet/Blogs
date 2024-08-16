const Joi = require("joi");

//map item validation rules
const mapItemValidation = async (field) => {
  const schema = Joi.object({
    id: Joi.string().required(),
    source: Joi.string().required(),
    target: Joi.string().required(),
    x: Joi.number().integer().required(),
    y: Joi.number().integer().required(),
    label: Joi.string().required(),
    lineId: Joi.string().required(),
  });
  try {
    return await schema.validateAsync(field, { abortEarly: false });
  } catch (err) {
    return err;
  }
};

module.exports = {
  mapItemValidation,
};
