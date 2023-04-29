import React, {useCallback, useEffect, useRef, useState} from "react";
import {Container, Sprite, Stage, useApp} from "@pixi/react";
import {OutlineFilter} from '@pixi/filter-outline';
import {BaseTexture, Rectangle, Texture} from "pixi.js";
import cursor from "../assets/cursor.png";
import duck from "../assets/duck.png";

const width = 1200;
const height = 800;
const backgroundColor = 0x505050;

function DraggableBox(
    {
        x = 0,
        y = 0,
        setOnBoxMove,
        box,
        texture,
        setCurrentlyDragging,
        sendMoveOnRelease,
        ...props
    }
) {
    const isDragging = React.useRef(false);
    const offset = React.useRef({x: 0, y: 0});
    const [position, setPosition] = React.useState({x, y})
    const [alpha, setAlpha] = React.useState(1);

    useEffect(() => {
        onOutsideChangePosition();
    }, [box]);

    function onOutsideChangePosition() {
        if (!isDragging.current) {
            setPosition({
                x: x,
                y: y
            })
        }
    }

    function onStart(e) {
        if (box.state === "moving" || box.state === "solved") {
            return
        }
        if (box.state === "released") {
            isDragging.current = true;

            const {x, y} = e.data.global;
            offset.current = {
                x: x - position.x,
                y: y - position.y
            };

            setAlpha(0.5);
            setOnBoxMove(() => onBoxMoveCallback);
        } else {
            console.error(`Box ${box.id} is in invalid state ${box.state}`);
        }
    }

    const onBoxMoveCallback = useCallback((outsideEvent) => {
        const {x, y} = outsideEvent.data.global;
        if (isDragging.current) {
            let newX = x - offset.current.x;
            let newY = y - offset.current.y;
            setPosition({
                x: newX,
                y: newY,
            });
            return {
                id: box.id,
                x: newX,
                y: newY,
                state: "moving",
            };
        }
    }, []);

    function onEnd() {
        isDragging.current = false;
        setOnBoxMove(null);
        setAlpha(1);
        sendMoveOnRelease({
            id: box.id,
            x: position.x,
            y: position.y,
            state: "released",
        });
    }

    let filters = [new OutlineFilter(2, 0x000000)]
    if (box.state === "solved") {
        filters = []
    } else if (box.state === "moving") {
        filters = [new OutlineFilter(2, 0xff0000)]
    }

    return (
        <Sprite
            {...props}
            alpha={alpha}
            position={position}
            texture={texture}
            width={100}
            height={100}
            zIndex={zIndex}
            eventMode='static'
            pointerdown={onStart}
            pointerup={onEnd}
            pointerupoutside={onEnd}
            filters={filters}
        />
    );
}

function Cursor({position}) {
    return (
        <Sprite
            position={position}
            texture={Texture.from(cursor)}
            width={50}
            height={50}
            eventMode='none'
            zIndex={99999999}
        />
    );
}

function ContainerWrapper({sendRequest, roomId, boxes, clients, clientId}) {
    const [onBoxMove, setOnBoxMove] = useState(null);

    const cursorPositionRef = useRef({x: 0, y: 0});
    const texturesRef = useRef(new Map());
    const draggingBoxRef = useRef(null);
    const lastZIndexRef = useRef(boxes.length);

    const app = useApp();

    useEffect(() => {
        const intervalId = setInterval(sendMove, 1000);
        return () => clearInterval(intervalId);
    }, []);

    function sendMove() {
        // TODO: don't send if cursor position hasn't changed
        sendRequest("move", {
            roomId: roomId,
            box: {...draggingBoxRef.current, z: lastZIndexRef.current++},
            cursor: cursorPositionRef.current,
        });
    }

    function sendMoveOnRelease(box) {
        draggingBoxRef.current = null;
        sendRequest("move", {
            roomId: roomId,
            box: {...box, z: lastZIndexRef.current++},
            cursor: cursorPositionRef.current,
        });
    }

    function movePlayer(e) {
        const {x, y} = e.data.global
        cursorPositionRef.current = {x: x, y: y};
        if (onBoxMove) {
            draggingBoxRef.current = onBoxMove(e); // {id, x, y, state}
        }
    }

    const texture = BaseTexture.from(duck);
    const amountVertical = 4;
    const amountHorizontal = 4;
    const pieceHeight = Math.floor(texture.height / amountVertical);
    const pieceWidth = Math.floor(texture.width / amountHorizontal);

    function getBoxPositionConsideringCurrentDragging(box) {
        let current = draggingBoxRef.current;
        return current && current.x === box.id ? {
            x: current.x,
            y: current.y
        } : {x: box.x, y: box.y};
    }

    return <Container
        sortableChildren={true}
        pointermove={movePlayer}
        eventMode="static"
        hitArea={app.screen}
    >
        {boxes.map((box) => {
            let boxId = box.id;
            const boxPos = getBoxPositionConsideringCurrentDragging(box);
            const pieceMapping = {
                id: boxId,
                x: (boxId % amountHorizontal) * pieceWidth,
                y: Math.floor(boxId / amountVertical) * pieceHeight
            };
            const rect = new Rectangle(
                pieceMapping.x,
                pieceMapping.y,
                pieceWidth,
                pieceHeight
            );
            const pieceTexture = new Texture(texture, rect);
            texturesRef.current.set(boxId, pieceTexture);
            return <DraggableBox
                key={boxId}
                x={boxPos.x}
                y={boxPos.y}
                setOnBoxMove={setOnBoxMove}
                box={box}
                texture={pieceTexture}
                sendMoveOnRelease={sendMoveOnRelease}
                zIndex={box.z}
            />;
        })}
        {clients.map((client) => {
            if (client.id === clientId) return null;
            return <Cursor key={client.id} position={client.cursor}/>
        })}
        <Sprite position={{x: 400, y: 200}} texture={Texture.WHITE} width={400} height={400} zIndex={-1}/>
    </Container>;
}

function Game({sendRequest, roomId, boxes, clients, clientId}) {
    return (
        <Stage width={width} height={height} options={{backgroundColor}}>
            <ContainerWrapper
                sendRequest={sendRequest}
                roomId={roomId}
                boxes={boxes}
                clients={clients}
                clientId={clientId}
            />
        </Stage>
    );
}

export default Game;