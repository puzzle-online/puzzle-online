import React, {useCallback, useEffect, useRef, useState} from "react";
import {Container, Sprite, Stage, useApp} from "@pixi/react";
import {BaseTexture, Rectangle, Texture} from "pixi.js";
import cursor from "../assets/cursor.png";
import duck from "../assets/duck.png";

const width = 1200;
const height = 800;
const backgroundColor = 0x505050;

let index = 1;


function DraggableBox({tint, x = 0, y = 0, cursorPosition, setOnBoxMove, boxId, boxes, texture, ...props}) {
    const isDragging = React.useRef(false);
    const offset = React.useRef({x: 0, y: 0});
    const [position, setPosition] = React.useState({x, y})
    const [alpha, setAlpha] = React.useState(1);
    const [zIndex, setZIndex] = React.useState(index);

    const onBoxMoveCallback = useCallback((outsideEvent) => {
        const {x, y} = outsideEvent.data.global;
        if (isDragging.current) {
            let newX = x - offset.current.x;
            let newY = y - offset.current.y;
            setPosition({
                x: newX,
                y: newY,
            })
            return {boxId: boxId, boxX: newX, boxY: newY};
        }
    }, []);


    useEffect(() => {
        onOutsideChangePosition();
    }, [boxes]);

    function onOutsideChangePosition() {
        if (!isDragging.current) {
            setPosition({
                x: x,
                y: y
            })
        }
    }

    function onStart(e) {
        const {x, y} = e.data.global;

        isDragging.current = true;

        offset.current = {
            x: x - position.x,
            y: y - position.y
        };

        setAlpha(0.5);
        setZIndex(index++);

        setOnBoxMove(() => onBoxMoveCallback);
    }

    function onEnd() {
        isDragging.current = false;
        setOnBoxMove(null);
        setAlpha(1);
    }

    return (
        <Sprite
            {...props}
            alpha={alpha}
            position={position}
            // texture={Texture.WHITE}
            texture={texture}
            width={100}
            height={100}
            zIndex={zIndex}
            // tint={tint}
            eventMode='static'
            pointerdown={onStart}
            pointerup={onEnd}
            pointerupoutside={onEnd}
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
            zIndex={9999}
        />
    );
}

function ContainerWrapper({sendRequest, roomId, boxes, clients, clientId}) {
    const [cursorPosition, setCursorPosition] = useState({x: 0, y: 0});
    const app = useApp();
    // const onBoxMove = useRef(null);
    const [onBoxMove, setOnBoxMove] = useState(null);
    const backlogRef = useRef({cursor: {x: 0, y: 0}, boxes: new Map()});
    const texturesRef = useRef(new Map());

    useEffect(() => {
        const intervalId = setInterval(sendBacklog, 1000);
        return () => clearInterval(intervalId);
    }, []);

    function sendBacklog() {
        const backlog = backlogRef.current;
        if (backlog.boxes.length > 0 || backlog.cursor.x !== cursorPosition.x || backlog.cursor.y !== cursorPosition.y) {
            const boxes = Array.from(backlog.boxes.entries()).map(([id, {x, y}]) => ({id, x, y}));
            sendRequest('move', {cursor: backlog.cursor, boxes, roomId: roomId});
            backlogRef.current = {cursor: backlog.cursor, boxes: new Map()};
        }
    }

    function movePlayer(e) {
        const {x, y} = e.data.global
        setCursorPosition({x: x, y: y});
        backlogRef.current.cursor = {x: x, y: y};
        if (onBoxMove) {
            const {boxId, boxX, boxY} = onBoxMove(e);
            backlogRef.current.boxes.set(boxId, {x: boxX, y: boxY});
        }
    }

    const texture = BaseTexture.from(duck);
    const amountVertical = 4;
    const amountHorizontal = 4;
    const pieceHeight = Math.floor(texture.height / amountVertical);
    const pieceWidth = Math.floor(texture.width / amountHorizontal);

    return <Container
        sortableChildren={true}
        pointermove={movePlayer}
        eventMode="static"
        hitArea={app.screen}
    >
        {boxes.map((box) => {
            let boxId = box.id;
            const backlogBox = backlogRef.current.boxes.get(boxId) ?? box;
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
                tint={0xff00ff}
                x={backlogBox.x}
                y={backlogBox.y}
                setOnBoxMove={setOnBoxMove}
                boxId={boxId}
                boxes={boxes}
                texture={pieceTexture}
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
            <ContainerWrapper sendRequest={sendRequest} roomId={roomId} boxes={boxes} clients={clients}
                              clientId={clientId}/>
        </Stage>
    );
}

export default Game;