import React, { useCallback, useState, useEffect } from "react";
import ReactFlow, {
  MiniMap,
  Controls,
  Background,
  useNodesState,
  useEdgesState,
  addEdge,
} from "reactflow";
import { useForm } from "react-hook-form";
import { TextField, Box, Button, Typography } from "@mui/material";
import Modal from "@mui/material/Modal";
import "reactflow/dist/style.css";
import { Handle, Position } from "reactflow";
import { MdDeleteOutline } from "react-icons/md";
import { FiEdit } from "react-icons/fi";
import {
  useDeleteMapItemMutation,
  useEditMapItemMutation,
  useGetAllMapItemsQuery,
} from "../store/Features/mapItem/mapItemApiSlice";
import { toast } from "react-toastify";

const handleStyle = { left: 10 };

const nodeTypes = {
  customNodes: CustomNodes,
};

export default function MainMindMap() {
  const { data, error, isLoading } = useGetAllMapItemsQuery("mapItems", {
    pollingInterval: 60000,
    refetchOnFocus: true,
    refetchOnMountOrArgChange: true,
  });

  const [nodes, setNodes, onNodesChange] = useNodesState([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);

  useEffect(() => {
    if (data) {
      const { entities } = data;
      const mapItems = Object.values(entities);

      const initialNodes = mapItems.map((item) => ({
        id: item.id,
        position: { x: item.x, y: item.y },
        data: { item, mapItems },
        type: "customNodes",
      }));

      const initialEdges = mapItems.map((item) => ({
        id: item.lineId,
        source: item.source,
        target: item.target,
      }));

      setNodes(initialNodes);
      setEdges(initialEdges);
    }
  }, [data, setNodes, setEdges]);

  const onConnect = useCallback(
    (params) => setEdges((eds) => addEdge(params, eds)),
    [setEdges]
  );

  return (
    <div
      sx={{ display: "flex", position: "relative" }}
      style={{ width: "100vw", height: "100vh" }}
    >
      <ReactFlow
        style={{ width: "100vw", height: "100vh" }}
        nodes={nodes}
        edges={edges}
        onConnect={onConnect}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        nodeTypes={nodeTypes}
      >
        <MiniMap />
        <Controls />
        <Background />
      </ReactFlow>
    </div>
  );
}

const style = {
  position: "absolute",
  top: "50%",
  left: "50%",
  transform: "translate(-50%, -50%)",
  width: 400,
  bgcolor: "background.paper",
  border: "2px solid #000",
  boxShadow: 24,
  p: 4,
};

function CustomNodes({ data, isConnectable }) {
  const { item, mapItems } = data;
  const { id, source, x, y, label, target, lineId } = item;

  const newArray = mapItems.filter((item) => item.id !== id);

  const [deleteMapItem] = useDeleteMapItemMutation();
  const [editMapItem] = useEditMapItemMutation();

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm();
  const [open, setOpen] = useState(false);
  const handleEdit = () => setOpen(true);
  const handleClose = () => setOpen(false);

  const handleCheck = useCallback(() => {}, []);

  const onChange = useCallback((evt) => {
    console.log(evt.target.value);
  }, []);

  return (
    <Box
      sx={{
        border: "2px solid #f9fbe7",
        width: "150px",
        background: "#f9fbe7",
        borderRadius: "10px",
      }}
    >
      <Handle
        type="target"
        position={Position.Top}
        isConnectable={isConnectable}
      />
      <Box sx={{ width: "100%" }}>
        <Modal
          open={open}
          onClose={handleClose}
          aria-labelledby="modal-modal-title"
          aria-describedby="modal-modal-description"
        >
          <Box sx={style}>
            <label htmlFor="">source</label>
            <select
              style={{ width: "100%", height: "40px" }}
              {...register("source")}
            >
              <option value={source}>{label}</option>
              {newArray.map((node) => (
                <option key={node.id} value={node.id}>
                  {node.label}
                </option>
              ))}
            </select>

            <label htmlFor="">positionX</label>
            <TextField
              type="text"
              name="x"
              defaultValue={x}
              style={{ width: "100%" }}
              {...register("x", {
                required: "x is required.",
                pattern: { value: /^\d+$/, message: "x must be numeric." },
              })}
            />
            {errors.x && <p className="errorMsg">{errors.x.message}</p>}
            <label htmlFor="">positionY</label>
            <TextField
              defaultValue={y}
              type="text"
              name="y"
              style={{ width: "100%" }}
              {...register("y", {
                required: "y is required.",
                pattern: { value: /^\d+$/, message: "y must be numeric." },
              })}
            />
            {errors.y && <p className="errorMsg">{errors.y.message}</p>}
            <label htmlFor="">label</label>
            <TextField
              defaultValue={label}
              type="text"
              name="label"
              style={{ width: "100%", position: "relative" }}
              {...register("label", {
                required: "label is required.",
                pattern: {
                  value: /\b[A-Za-z]+\b/,
                  message: "label must be alphabetic.",
                },
              })}
            />
            {errors.label && <p className="errorMsg">{errors.label.message}</p>}

            <Button
              variant="contained"
              color="primary"
              style={{
                cursor: "pointer",
                width: "100%",
              }}
              sx={{ marginTop: "20px" }}
              size="1rem"
              onClick={handleSubmit(async (data) => {
                try {
                  const res = await editMapItem({
                    id,
                    source: data.source,
                    target,
                    x: Number(data.x),
                    y: Number(data.y),
                    label: data.label,
                    lineId,
                  }).unwrap();

                  setOpen(false);
                  toast.success(`${res.message}`, {
                    position: "top-right",
                    autoClose: 5000,
                    hideProgressBar: false,
                    closeOnClick: true,
                    pauseOnHover: true,
                    draggable: true,
                    progress: undefined,
                    theme: "light",
                  });

                  // setTimeout(() => {
                  //   window.location.reload();
                  // }, 5000);
                } catch (error) {
                  let msg =
                    error.message ||
                    (error.data && error.data.message) ||
                    "An error occurred";
                  toast.error(`${msg}`, {
                    position: "top-right",
                    autoClose: 5000,
                    hideProgressBar: false,
                    closeOnClick: true,
                    pauseOnHover: true,
                    draggable: true,
                    progress: undefined,
                    theme: "light",
                  });
                }
              })}
            >
              Update
            </Button>
          </Box>
        </Modal>
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-around",
            alignItems: "center",
            width: "100%",
            height: "100%",
          }}
        >
          <FiEdit
            color="black"
            size="1rem"
            onClick={handleEdit}
            style={{
              cursor: "pointer",
              marginLeft: "10px",
            }}
          />
          <Typography sx={{ textAlign: "center" }}>{label}</Typography>
          <MdDeleteOutline
            style={{
              cursor: "pointer",
              marginRight: "10px",
            }}
            color="black"
            size="1rem"
            onClick={async () => {
              try {
                const res = await deleteMapItem({ id }).unwrap();

                toast.success(`${res.message}`, {
                  position: "top-right",
                  autoClose: 5000,
                  hideProgressBar: false,
                  closeOnClick: true,
                  pauseOnHover: true,
                  draggable: true,
                  progress: undefined,
                  theme: "light",
                });

                // setTimeout(() => {
                //   window.location.reload();
                // }, 5000);
              } catch (error) {
                let msg =
                  error.message ||
                  (error.data && error.data.message) ||
                  "An error occurred";
                toast.error(`${msg}`, {
                  position: "top-right",
                  autoClose: 5000,
                  hideProgressBar: false,
                  closeOnClick: true,
                  pauseOnHover: true,
                  draggable: true,
                  progress: undefined,
                  theme: "light",
                });
              }
            }}
          />
        </Box>
      </Box>
      <Handle
        type="source"
        position={Position.Bottom}
        id="a"
        style={handleStyle}
        isConnectable={isConnectable}
      />
      <Handle
        type="source"
        position={Position.Bottom}
        id="b"
        isConnectable={isConnectable}
      />
    </Box>
  );
}
