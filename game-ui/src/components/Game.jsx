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

    const textureRef = useRef(Texture.from(duck));

    // useEffect(() => {
    //     const texture = Texture.from(duck);
    //     const {width: imageWidth, height: imageHeight} = texture;
    //     const pieceWidth = 100;
    //     const pieceHeight = 100;
    //
    //     // Calculate the position and dimensions of the portion of the image to be used for this piece
    //     const pieceX = x;
    //     const pieceY = y;
    //     const pieceRect = new Rectangle(
    //         pieceX / imageWidth,
    //         pieceY / imageHeight,
    //         pieceWidth / imageWidth,
    //         pieceHeight / imageHeight
    //     );
    //
    //     // Create a new texture that only shows the portion of the image for this piece
    //     const pieceTexture = new Texture(texture.baseTexture, pieceRect);
    //     textureRef.current = pieceTexture;
    // }, [x, y]);

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
            texture={textureRef.current}
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

    function movePlayer(e) {
        const {x, y} = e.data.global
        setCursorPosition({x: x, y: y});
        // TODO: use backlog of events instead of sending every event (or send on onEnd)
        const move = {cursor: {x: x, y: y}, roomId: roomId};
        move.box = null;
        if (onBoxMove) {
            const {boxId, boxX, boxY} = onBoxMove(e);
            move.box = {id: boxId, x: boxX, y: boxY};
        }
        sendRequest('move', move);
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
            <ContainerWrapper sendRequest={sendRequest} roomId={roomId} boxes={boxes} clients={clients} clientId={clientId}/>
        </Stage>
    );
}

export default Game;