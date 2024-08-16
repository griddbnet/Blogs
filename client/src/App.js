import React from "react";
import "@fontsource/roboto/300.css";
import "@fontsource/roboto/400.css";
import "@fontsource/roboto/500.css";
import "@fontsource/roboto/700.css";
import { Box, CssBaseline, ThemeProvider } from "@mui/material";
import theme from "./config/theme";
import MainMindMap from "./components/MainMindMap";
import FormInput from "./components/FormInput";
import { Provider } from "react-redux";
import store from "./store/store";
// import { PersistGate } from "redux-persist/integration/react";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

function App() {
  return (
    <ThemeProvider theme={theme}>
      <Provider store={store}>
        {/* <PersistGate loading={null} persistor={persistor}> */}
        <Box component="main" style={styles.mainContianer} display="flex">
          <MainMindMap />
          <FormInput />
        </Box>
        {/* </PersistGate> */}
      </Provider>
      <CssBaseline />
      <ToastContainer />
    </ThemeProvider>
  );
}

/** @type {import("@mui/material").SxProps} */
const styles = {
  mainContianer: {
    padding: 0,
    margin: 0,
    boxSizing: "border-box",
    backgroundColor: "#E5E5E5",
  },
};

export default App;
