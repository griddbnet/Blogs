const {
  addMealItem,
  mapItemDetails,
  editMapItem,
  deleteMapItem,
  getAllMapItems,
} = require("../controllers/mindMapController");
const router = require("express").Router();

router.post("/add-map-item", addMealItem);
router.get("/map-detail/:id", mapItemDetails);
router.put("/edit-map-item/:id", editMapItem);
router.delete("/delete-map-item/:id", deleteMapItem);
router.get("/all-map-items", getAllMapItems);

module.exports = router;
