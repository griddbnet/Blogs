export const getValidDayIndices = (arrayDays) => {
  const Days = [
    "monday",
    "tuesday",
    "wednesday",
    "thursday",
    "friday",
    "saturday",
    "sunday",
  ];

  const splitDays = arrayDays.split(";");
  const validDayIndices = [];

  splitDays.forEach((day) => {
    const index = Days.indexOf(day);
    if (index !== -1) {
      validDayIndices.push(index);
    }
  });

  return validDayIndices;
};
