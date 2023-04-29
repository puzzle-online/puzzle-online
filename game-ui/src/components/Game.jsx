import React, {useCallback, useEffect, useRef, useState} from "react";
import {Sprite, Stage, Container, useApp} from "@pixi/react";
import {Rectangle, Texture} from "pixi.js";
import cursor from "../assets/cursor.png";
import duck from "../assets/duck.png";

const width = 1200;
const height = 800;
const backgroundColor = 0x505050;

let index = 1;


function DraggableBox({tint, x = 0, y = 0, cursorPosition, setOnBoxMove, boxId, boxes, ...props}) {
    const isDragging = React.useRef(false);
    const offset = React.useRef({x: 0, y: 0});
    const [position, setPosition] = React.useState({x, y})
    const [alpha, setAlpha] = React.useState(1);
    const [zIndex, setZIndex] = React.useState(index);

    const texture = Texture.from(duck);
    console.log("texture ", texture.height, texture.width)

    const mapping = [
        {id: 0, x: 0, y: 0},
        {id: 1, x: 100, y: 0},
        {id: 2, x: 200, y: 0},
        {id: 3, x: 300, y: 0},
        {id: 4, x: 0, y: 100},
        {id: 5, x: 100, y: 100},
        {id: 6, x: 200, y: 100},
        {id: 7, x: 300, y: 100},
        {id: 8, x: 0, y: 200},
        {id: 9, x: 100, y: 200},
        {id: 10, x: 200, y: 200},
        {id: 11, x: 300, y: 200},
        {id: 12, x: 0, y: 300},
        {id: 13, x: 100, y: 300},
        {id: 14, x: 200, y: 300},
        {id: 15, x: 300, y: 300}
    ]

    console.log('boxId', boxId)
    console.log('mapping', mapping[boxId])

    texture.frame = new Rectangle(mapping[boxId].x, mapping[boxId].y, 100, 100);

    console.log("frame:", texture.frame)

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

    useEffect(() => {
        const intervalId = setInterval(sendBacklog, 50);
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

    return <Container
        sortableChildren={true}
        pointermove={movePlayer}
        eventMode="static"
        hitArea={app.screen}
    >
        {boxes.map((box) => {
            return <DraggableBox
                key={box.id}
                tint={0xff00ff}
                x={box.x}
                y={box.y}
                setOnBoxMove={setOnBoxMove}
                boxId={box.id}
                boxes={boxes}
            />
        })}
        {/*<Cursor position={cursorPosition}/>*/}
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