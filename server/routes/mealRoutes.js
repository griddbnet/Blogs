const {
  addMealPlan,
  mealPlanDetails,
  editMealPlan,
  deleteMealPlan,
  getAllMealPlans
} = require("../controllers/mealController")
const router = require("express").Router()

router.post("/add-meal", addMealPlan)
router.get("/meal-detail/:id", mealPlanDetails)
router.put("/edit-meal/:id", editMealPlan)
router.delete("/delete-meal/:id", deleteMealPlan)
router.get("/all-meals", getAllMealPlans)

module.exports = router
