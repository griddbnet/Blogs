const Details = [
  {
    inputType: "select",
    name: "Source"
    // errorMessage: "Please enter a valid source.",
    // errorParams: {
    //   required: "source is required.",
    //   pattern: {
    //     value: /^.*$/,
    //     message: "Source must have an alphanumeric value."
    //   }
    // }
  },
  {
    inputType: "text",
    name: "PositionX",
    errorMessage: "Please enter a valid x.",
    errorParams: {
      required: "x is required.",
      pattern: { value: /^\d+$/, message: "x must be numeric." }
    }
  },
  {
    inputType: "text",
    name: "PositionY",
    errorMessage: "Please enter a valid y.",
    errorParams: {
      required: "y is required.",
      pattern: { value: /^\d+$/, message: "y must be numeric." }
    }
  },
  {
    inputType: "text",
    name: "Label",
    errorMessage: "Please enter a valid label.",
    errorParams: {
      required: "label is required.",
      pattern: {
        value: "\\b[A-Za-z]+\\b",
        message: "label must be alphabets."
      }
    }
  }
]

export { Details }
