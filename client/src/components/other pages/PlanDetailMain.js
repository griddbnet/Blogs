import React from "react"
import { Details, food } from "../assets/data"
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Box,
  Typography
} from "@mui/material"
import ExpandMoreIcon from "@mui/icons-material/ExpandMore"
import Weeks from "./Weeks"
import Foods from "./Foods"

const PlanDetailMain = () => {
  return (
    <Box sx={{ padding: { xs: "20px 0", md: "0" }, display: { md: "none" } }}>
      <Accordion
        sx={{
          padding: { xs: "10px 0", md: "0" },
          margin: { xs: "25px 0", md: "0" }
        }}
        variant="plain"
      >
        <AccordionSummary
          expandIcon={
            <ExpandMoreIcon sx={{ color: "rgba(255, 255, 255, 1)" }} />
          }
          aria-controls="panel1a-content"
          id="panel1a-header"
          sx={{
            backgroundColor: "rgba(135, 197, 49, 1)",
            color: "rgba(255, 255, 255, 1)",
            margin: "0 20px",
            padding: "0 30px",
            marginBottom: "10px"
          }}
        >
          <Typography>Monday</Typography>
        </AccordionSummary>
        <AccordionDetails>
          {Details.map((item, i) => (
            <Weeks key={i} {...item} />
          ))}
          <Box
            className="foods"
            sx={{
              display: "flex",
              gap: 1,
              pl: 2,
              width: "100%",
              scrollSnapType: "x mandatory",
              overflowX: "auto",
              whiteSpace: "nowrap",
              "& > *": {
                scrollSnapAlign: "center"
              },
              "::-webkit-scrollbar": { display: "none" }
            }}
          >
            {food.map((item, i) => (
              <Foods key={i} {...item} />
            ))}
          </Box>
        </AccordionDetails>
      </Accordion>

      <Accordion
        sx={{
          padding: { xs: "10px 0", md: "0" },
          margin: { xs: "25px 0", md: "0" }
        }}
        variant="plain"
      >
        <AccordionSummary
          expandIcon={
            <ExpandMoreIcon sx={{ color: "rgba(255, 255, 255, 1)" }} />
          }
          aria-controls="panel1a-content"
          id="panel1a-header"
          sx={{
            backgroundColor: "rgba(135, 197, 49, 1)",
            color: "rgba(255, 255, 255, 1)",
            margin: "0 20px",
            padding: "0 30px",
            marginBottom: "10px"
          }}
        >
          <Typography>Tuesday</Typography>
        </AccordionSummary>
        <AccordionDetails>
          {Details.map((item, i) => (
            <Weeks key={i} {...item} />
          ))}
          <Box
            className="foods"
            sx={{
              display: "flex",
              gap: 1,
              pl: 2,
              width: "100%",
              scrollSnapType: "x mandatory",
              overflowX: "auto",
              whiteSpace: "nowrap",
              "& > *": {
                scrollSnapAlign: "center"
              },
              "::-webkit-scrollbar": { display: "none" }
            }}
          >
            {food.map((item, i) => (
              <Foods key={i} {...item} />
            ))}
          </Box>
        </AccordionDetails>
      </Accordion>

      <Accordion
        sx={{
          padding: { xs: "10px 0", md: "0" },
          margin: { xs: "25px 0", md: "0" }
        }}
        variant="plain"
      >
        <AccordionSummary
          expandIcon={
            <ExpandMoreIcon sx={{ color: "rgba(255, 255, 255, 1)" }} />
          }
          aria-controls="panel1a-content"
          id="panel1a-header"
          sx={{
            backgroundColor: "rgba(135, 197, 49, 1)",
            color: "rgba(255, 255, 255, 1)",
            margin: "0 20px",
            padding: "0 30px",
            marginBottom: "10px"
          }}
        >
          <Typography>Wednesday</Typography>
        </AccordionSummary>
        <AccordionDetails>
          {Details.map((item, i) => (
            <Weeks key={i} {...item} />
          ))}
          <Box
            className="foods"
            sx={{
              display: "flex",
              gap: 1,
              pl: 2,
              width: "100%",
              scrollSnapType: "x mandatory",
              overflowX: "auto",
              whiteSpace: "nowrap",
              "& > *": {
                scrollSnapAlign: "center"
              },
              "::-webkit-scrollbar": { display: "none" }
            }}
          >
            {food.map((item, i) => (
              <Foods key={i} {...item} />
            ))}
          </Box>
        </AccordionDetails>
      </Accordion>

      <Accordion
        sx={{
          padding: { xs: "10px 0", md: "0" },
          margin: { xs: "25px 0", md: "0" }
        }}
        variant="plain"
      >
        <AccordionSummary
          expandIcon={
            <ExpandMoreIcon sx={{ color: "rgba(255, 255, 255, 1)" }} />
          }
          aria-controls="panel1a-content"
          id="panel1a-header"
          sx={{
            backgroundColor: "rgba(135, 197, 49, 1)",
            color: "rgba(255, 255, 255, 1)",
            margin: "0 20px",
            padding: "0 30px",
            marginBottom: "10px"
          }}
        >
          <Typography>Thursday</Typography>
        </AccordionSummary>
        <AccordionDetails>
          {Details.map((item, i) => (
            <Weeks key={i} {...item} />
          ))}
          <Box
            className="foods"
            sx={{
              display: "flex",
              gap: 1,
              pl: 2,
              width: "100%",
              scrollSnapType: "x mandatory",
              overflowX: "auto",
              whiteSpace: "nowrap",
              "& > *": {
                scrollSnapAlign: "center"
              },
              "::-webkit-scrollbar": { display: "none" }
            }}
          >
            {food.map((item, i) => (
              <Foods key={i} {...item} />
            ))}
          </Box>
        </AccordionDetails>
      </Accordion>

      <Accordion
        sx={{
          padding: { xs: "10px 0", md: "0" },
          margin: { xs: "25px 0", md: "0" }
        }}
        variant="plain"
      >
        <AccordionSummary
          expandIcon={
            <ExpandMoreIcon sx={{ color: "rgba(255, 255, 255, 1)" }} />
          }
          aria-controls="panel1a-content"
          id="panel1a-header"
          sx={{
            backgroundColor: "rgba(135, 197, 49, 1)",
            color: "rgba(255, 255, 255, 1)",
            margin: "0 20px",
            padding: "0 30px",
            marginBottom: "10px"
          }}
        >
          <Typography>Friday</Typography>
        </AccordionSummary>
        <AccordionDetails>
          {Details.map((item, i) => (
            <Weeks key={i} {...item} />
          ))}
          <Box
            className="foods"
            sx={{
              display: "flex",
              gap: 1,
              pl: 2,
              width: "100%",
              scrollSnapType: "x mandatory",
              overflowX: "auto",
              whiteSpace: "nowrap",
              "& > *": {
                scrollSnapAlign: "center"
              },
              "::-webkit-scrollbar": { display: "none" }
            }}
          >
            {food.map((item, i) => (
              <Foods key={i} {...item} />
            ))}
          </Box>
        </AccordionDetails>
      </Accordion>

      <Accordion
        sx={{
          padding: { xs: "10px 0", md: "0" },
          margin: { xs: "25px 0", md: "0" }
        }}
        variant="plain"
      >
        <AccordionSummary
          expandIcon={
            <ExpandMoreIcon sx={{ color: "rgba(255, 255, 255, 1)" }} />
          }
          aria-controls="panel1a-content"
          id="panel1a-header"
          sx={{
            backgroundColor: "rgba(135, 197, 49, 1)",
            color: "rgba(255, 255, 255, 1)",
            margin: "0 20px",
            padding: "0 30px",
            marginBottom: "10px"
          }}
        >
          <Typography>Saturday</Typography>
        </AccordionSummary>
        <AccordionDetails>
          {Details.map((item, i) => (
            <Weeks key={i} {...item} />
          ))}
          <Box
            className="foods"
            sx={{
              display: "flex",
              gap: 1,
              pl: 2,
              width: "100%",
              scrollSnapType: "x mandatory",
              overflowX: "auto",
              whiteSpace: "nowrap",
              "& > *": {
                scrollSnapAlign: "center"
              },
              "::-webkit-scrollbar": { display: "none" }
            }}
          >
            {food.map((item, i) => (
              <Foods key={i} {...item} />
            ))}
          </Box>
        </AccordionDetails>
      </Accordion>

      <Accordion
        sx={{
          padding: { xs: "10px 0", md: "0" },
          margin: { xs: "25px 0", md: "0" }
        }}
        variant="plain"
      >
        <AccordionSummary
          expandIcon={
            <ExpandMoreIcon sx={{ color: "rgba(255, 255, 255, 1)" }} />
          }
          aria-controls="panel1a-content"
          id="panel1a-header"
          sx={{
            backgroundColor: "rgba(135, 197, 49, 1)",
            color: "rgba(255, 255, 255, 1)",
            margin: "0 20px",
            padding: "0 30px",
            marginBottom: "10px"
          }}
        >
          <Typography>Sunday</Typography>
        </AccordionSummary>
        <AccordionDetails>
          {Details.map((item, i) => (
            <Weeks key={i} {...item} />
          ))}
          <Box
            className="foods"
            sx={{
              display: "flex",
              gap: 1,
              pl: 2,
              width: "100%",
              scrollSnapType: "x mandatory",
              overflowX: "auto",
              whiteSpace: "nowrap",
              "& > *": {
                scrollSnapAlign: "center"
              },
              "::-webkit-scrollbar": { display: "none" }
            }}
          >
            {food.map((item, i) => (
              <Foods key={i} {...item} />
            ))}
          </Box>
        </AccordionDetails>
      </Accordion>
    </Box>
  )
}

export default PlanDetailMain
