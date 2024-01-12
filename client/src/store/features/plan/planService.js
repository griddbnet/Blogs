const API_URL = "http://localhost:4000/api/";

const addMealPlan = async (data) => {
  const rawResponse = await fetch(`${API_URL}add-meal`, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
  const content = await rawResponse.json();

  return content;
};

const getMealPlan = async (id) => {
  const rawResponse = await fetch(`${API_URL}meal-detail/${id}`);
  const content = await rawResponse.json();

  return content;
};

const editMealPlan = async (data, id) => {
  const rawResponse = await fetch(`${API_URL}edit-meal/${id}`, {
    method: "PUT",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
    },
    body: JSON.stringify(data),
  });
  const content = await rawResponse.json();

  return content;
};

const deleteMealPlan = async (id) => {
  const rawResponse = await fetch(`${API_URL}delete-meal/${id}`, {
    method: "DELETE",
  });
  const content = await rawResponse.json();

  return content;
};

const getAllMealPlans = async () => {
  const rawResponse = await fetch(`${API_URL}all-meals`);
  const content = await rawResponse.json();

  return content;
};

export {
  addMealPlan,
  getMealPlan,
  editMealPlan,
  deleteMealPlan,
  getAllMealPlans,
};
