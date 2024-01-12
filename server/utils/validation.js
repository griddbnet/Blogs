const Joi = require("joi");

//meal plan validation rules
const mealPlanValidation = async (field) => {
  const schema = Joi.object({
    title: Joi.string().required(),
    calories: Joi.number().integer().required(),
    fat: Joi.number().integer().required(),
    cabs: Joi.number().integer().required(),
    protein: Joi.number().integer().required(),
    days: Joi.array()
      .items(
        Joi.string().valid(
          "sunday",
          "monday",
          "tuesday",
          "wednesday",
          "thursday",
          "friday",
          "saturday"
        )
      )
      .max(7)
      .min(1)
      .required(),
    breakfast: Joi.string().required(),
    lunch: Joi.string().required(),
    dinner: Joi.string().required(),
    snack1: Joi.string().required(),
    snack2: Joi.string().required(),
    snack3: Joi.string().required(),
  });
  try {
    return await schema.validateAsync(field, { abortEarly: false });
  } catch (err) {
    return err;
  }
};

module.exports = {
  mealPlanValidation,
};
