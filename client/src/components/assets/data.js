import rectangleorange from "../assets/images/Rectangle orange.png";
import rectanglered from "../assets/images/Rectangle red.png";
import rectangleblue from "../assets/images/Rectangle blue.png";
import rectanglegreen from "../assets/images/Rectangle green.png";
import healingTumeric from "../assets/images/healing-turmeric-cauliflower-soup.png";

const Days = [
  "monday",
  "tuesday",
  "wednesday",
  "thursday",
  "friday",
  "saturday",
  "sunday",
];
const Date = [
  { day: "Monday" },
  { day: "Tuesday" },
  { day: "Wednesday" },
  { day: "Thursday" },
  { day: "Friday" },
  { day: "Saturday" },
  { day: "Sunday" },
];

const Details = [
  { color: rectangleorange, diet: "Calories", number: "155g" },
  { color: rectanglered, diet: "Fat", number: "94g" },
  { color: rectangleblue, diet: "Carbs", number: "117g" },
  { color: rectanglegreen, diet: "Fiber", number: "23g" },
  { color: rectangleblue, diet: "Sugar", number: "63g" },
  { color: rectanglegreen, diet: "Protein", number: "73g" },
];

const food = [
  {
    heading: "Breakfast",
    image: healingTumeric,
    name: "Pineapple Turmeric Cauliflower Porridqe",
  },
  {
    heading: "Snack-1",
    image: healingTumeric,
    name: "Pineapple Turmeric Cauliflower Porridqe",
  },
  {
    heading: "Lunch",
    image: healingTumeric,
    name: "Pineapple Turmeric Cauliflower Porridqe",
  },
  {
    heading: "Snack-2",
    image: healingTumeric,
    name: "Pineapple Turmeric Cauliflower Porridqe",
  },
  {
    heading: "Dinner",
    image: healingTumeric,
    name: "Pineapple Turmeric Cauliflower Porridqe",
  },
  {
    heading: "Snack-3",
    image: healingTumeric,
    name: "Pineapple Turmeric Cauliflower Porridqe",
  },
];

export { Days, Details, food, Date };
