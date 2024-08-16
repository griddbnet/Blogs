const express = require("express");
const morgan = require("morgan");
const app = express();
const cors = require("cors");

const PORT = 4000 || process.env.PORT;

app.use(morgan("dev"));
app.use(express.json());
app.use(express.urlencoded({ extended: false }));
app.use(cors("*"));

app.get("/", (req, res) => {
  res.send("GridDB mind map Backend API");
});

app.use("/api", require("./routes/mindMapRoutes"));

app.listen(PORT, () => {
  console.log(`Server started on ${PORT}`);
});
